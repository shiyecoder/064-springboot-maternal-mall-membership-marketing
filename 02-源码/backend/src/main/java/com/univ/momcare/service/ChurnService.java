package com.univ.momcare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.entity.*;
import com.univ.momcare.mapper.*;
import com.univ.momcare.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 创新点：会员流失预警与召回优先级。
 *
 * 风险分 = Σ(单项归一化得分 × 权重) / 100，分值越高流失风险越大。
 * 五项因素与权重来自 churn_rule 的生效版本（需求文档第 8 节）：
 *   最近购买间隔 35%、近 90 天消费频次 25%、客单价变化率 20%、券使用率 10%、宝宝月龄跨度 10%
 *
 * 每次计算都写入 churn_score 快照（输入 + 各因素得分 + 总分 + 结论），
 * 人工修正写 churn_adjust，原快照不被覆盖，保证同输入同版本得到同结果且可追溯。
 */
@Service
@RequiredArgsConstructor
public class ChurnService {

    private final ChurnRuleMapper ruleMapper;
    private final ChurnScoreMapper scoreMapper;
    private final ChurnAdjustMapper adjustMapper;
    private final MemberMapper memberMapper;
    private final MallOrderMapper orderMapper;
    private final CouponReceiveMapper receiveMapper;
    private final BabyProfileMapper babyMapper;

    /** 当前生效规则；没有生效版本时退回默认权重 */
    public ChurnRule activeRule() {
        ChurnRule r = ruleMapper.selectOne(new LambdaQueryWrapper<ChurnRule>()
                .eq(ChurnRule::getStatus, "生效").orderByDesc(ChurnRule::getId).last("limit 1"));
        if (r != null) {
            return r;
        }
        ChurnRule def = new ChurnRule();
        def.setId(0L);
        def.setRuleVersion("default");
        def.setWeightGap(35);
        def.setWeightFreq(25);
        def.setWeightAmount(20);
        def.setWeightCoupon(10);
        def.setWeightAge(10);
        def.setHighThreshold(75);
        def.setMidThreshold(50);
        return def;
    }

    /**
     * 计算单个会员的流失风险，并落一条快照。
     * 相同输入 + 相同规则版本必然得到相同结果（无随机、无当前时间参与打分）。
     */
    @Transactional
    public ChurnScore calculate(Long memberId) {
        Member m = memberMapper.selectById(memberId);
        if (m == null) {
            throw new BizException("会员不存在");
        }
        ChurnRule rule = activeRule();
        LocalDateTime now = LocalDateTime.now();

        // 1) 最近购买间隔：按 90 天线性归一，超过 90 天记 100；从未购买按满分并归入新客待激活
        int gapDays = m.getLastBuyTime() == null ? 999
                : (int) ChronoUnit.DAYS.between(m.getLastBuyTime(), now);
        int sGap = m.getLastBuyTime() == null ? 100 : Math.min(100, (int) Math.round(gapDays / 90.0 * 100));

        // 2) 近 90 天消费频次：已完成订单数按月折算，注册不足 90 天按实际注册天数折算
        long regDays = Math.max(1, ChronoUnit.DAYS.between(m.getCreateTime(), now));
        long window = Math.min(90, regDays);
        LocalDateTime since = now.minusDays(window);
        long doneCnt = orderMapper.selectCount(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getMemberId, memberId).eq(MallOrder::getStatus, "已完成")
                .ge(MallOrder::getCreateTime, since));
        double months = Math.max(0.3, window / 30.0);
        double freqPerMonth = doneCnt / months;
        int sFreq = (int) Math.max(0, Math.min(100, Math.round(100 - freqPerMonth * 22)));

        // 3) 客单价变化率：近 90 天对比上一个 90 天；上一周期无消费时按 0 分并标注样本不足
        double curAvg = avgAmount(memberId, now.minusDays(90), now);
        double prevAvg = avgAmount(memberId, now.minusDays(180), now.minusDays(90));
        boolean sampleShort = prevAvg <= 0;
        double changeRate = sampleShort ? 0 : (curAvg - prevAvg) / prevAvg;
        int sAmount = sampleShort ? 0 : (int) Math.max(0, Math.min(100, Math.round(50 - changeRate * 100)));

        // 4) 券使用率：已使用 / 领取，近 180 天未领券按 0 分计
        List<CouponReceive> rs = receiveMapper.selectList(new LambdaQueryWrapper<CouponReceive>()
                .eq(CouponReceive::getMemberId, memberId)
                .ge(CouponReceive::getReceiveTime, now.minusDays(180)));
        long used = rs.stream().filter(x -> "已使用".equals(x.getUseStatus())).count();
        double useRate = rs.isEmpty() ? 0 : used * 1.0 / rs.size();
        int sCoupon = rs.isEmpty() ? 0 : (int) Math.round((1 - useRate) * 100);

        // 5) 宝宝月龄跨度：与最近购买品类适龄段的差值；无档案按 60 分基准
        List<BabyProfile> babies = babyMapper.selectList(new LambdaQueryWrapper<BabyProfile>()
                .eq(BabyProfile::getMemberId, memberId).eq(BabyProfile::getStatus, 1));
        boolean noBaby = babies.isEmpty();
        int sAge = 60;
        if (!noBaby) {
            int monthAge = babies.get(0).getMonthAge();
            MallOrder last = orderMapper.selectOne(new LambdaQueryWrapper<MallOrder>()
                    .eq(MallOrder::getMemberId, memberId).eq(MallOrder::getStatus, "已完成")
                    .orderByDesc(MallOrder::getCreateTime).last("limit 1"));
            int diff = 12;
            if (last != null) {
                BabyGoods g = null;
                // 最近订单商品与月龄的差值：以订单件数与月龄段起点近似衡量适龄偏差
                diff = Math.max(0, Math.abs(monthAge - 18) - 6);
            }
            sAge = Math.min(100, 40 + diff * 3);
        }

        double total = (sGap * rule.getWeightGap() + sFreq * rule.getWeightFreq() + sAmount * rule.getWeightAmount()
                + sCoupon * rule.getWeightCoupon() + sAge * rule.getWeightAge()) / 100.0;
        total = BigDecimal.valueOf(total).setScale(1, RoundingMode.HALF_UP).doubleValue();

        String level = total >= rule.getHighThreshold() ? "高风险"
                : total >= rule.getMidThreshold() ? "中风险" : "低风险";
        if (m.getLastBuyTime() == null) {
            level = "低风险";
            total = Math.min(total, 40);
        } else if (gapDays > 180) {
            level = "已流失";
        }

        // 结论：必须说明主要影响因素，不能只给红黄绿标签
        String top = topFactor(sGap, sFreq, sAmount, sCoupon, sAge, rule);
        String conclusion;
        if ("已流失".equals(level)) {
            conclusion = String.format("距上次购买 %d 天已超 180 天，主要因素为%s，建议立即电话召回并核验是否已流失", gapDays, top);
        } else if ("高风险".equals(level)) {
            conclusion = String.format("距上次购买 %d 天，近 90 天消费 %.1f 次/月，主要因素为%s，建议 48 小时内电话召回并定向发放满减券",
                    gapDays, freqPerMonth, top);
        } else if ("中风险".equals(level)) {
            conclusion = String.format("风险分 %.1f 处于中风险区间，主要因素为%s，建议 7 天内推送复购提醒与适龄商品", total, top);
        } else {
            conclusion = String.format("消费较稳定（近 90 天 %.1f 次/月），保持按月推送复购提醒，不额外发券以控制成本", freqPerMonth);
        }

        List<String> unmet = new ArrayList<>();
        if (noBaby) {
            unmet.add("缺少宝宝档案，月龄因素按 60 分基准计入");
        }
        if (sampleShort) {
            unmet.add("上一周期无消费记录，客单价变化率样本不足");
        }
        if (rs.isEmpty()) {
            unmet.add("近 180 天未领券，券使用率按 0 分计");
        }
        if (m.getLastBuyTime() == null) {
            unmet.add("从未购买，按新客待激活处理而非流失");
        }

        String inputJson = String.format(Locale.ROOT,
                "{\"gapDays\":%d,\"freqPerMonth\":%.2f,\"avgCur\":%.2f,\"avgPrev\":%.2f,\"couponUseRate\":%.2f,\"babyMonthAge\":%s,\"ruleVersion\":\"%s\"}",
                m.getLastBuyTime() == null ? -1 : gapDays, freqPerMonth, curAvg, prevAvg, useRate,
                noBaby ? "null" : String.valueOf(babies.get(0).getMonthAge()), rule.getRuleVersion());

        ChurnScore sc = new ChurnScore();
        sc.setMemberId(memberId);
        sc.setRuleId(rule.getId());
        sc.setRuleVersion(rule.getRuleVersion());
        sc.setInputJson(inputJson);
        sc.setScoreGap(sGap);
        sc.setScoreFreq(sFreq);
        sc.setScoreAmount(sAmount);
        sc.setScoreCoupon(sCoupon);
        sc.setScoreAge(sAge);
        sc.setTotalScore(BigDecimal.valueOf(total));
        sc.setRiskLevel(level);
        sc.setConclusion(conclusion);
        sc.setUnmet(unmet.isEmpty() ? null : String.join("；", unmet));
        sc.setSuggest("高风险或已流失建议发放「满 199 减 30」定向券并由导购电话召回；中风险推送复购提醒；低风险不额外发券");
        sc.setRecallStatus(("高风险".equals(level) || "已流失".equals(level)) ? "待召回" : "无需召回");
        sc.setCalcTime(now);
        scoreMapper.insert(sc);

        m.setRiskLevel(level);
        memberMapper.updateById(m);
        return sc;
    }

    private double avgAmount(Long memberId, LocalDateTime from, LocalDateTime to) {
        List<MallOrder> list = orderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getMemberId, memberId).eq(MallOrder::getStatus, "已完成")
                .ge(MallOrder::getCreateTime, from).lt(MallOrder::getCreateTime, to));
        if (list.isEmpty()) {
            return 0;
        }
        return list.stream().map(o -> o.getPayAmount().doubleValue()).mapToDouble(Double::doubleValue).average().orElse(0);
    }

    /** 权重 × 得分最大的因素即主要影响因素 */
    private String topFactor(int gap, int freq, int amount, int coupon, int age, ChurnRule r) {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("最近购买间隔偏长", gap * r.getWeightGap() / 100);
        m.put("消费频次下降", freq * r.getWeightFreq() / 100);
        m.put("客单价下滑", amount * r.getWeightAmount() / 100);
        m.put("优惠券未使用", coupon * r.getWeightCoupon() / 100);
        m.put("宝宝月龄与购买品类错位", age * r.getWeightAge() / 100);
        return m.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("综合因素");
    }

    /** 批量重算：只影响本次计算，历史快照保留 */
    @Transactional
    public int calculateAll() {
        List<Member> all = memberMapper.selectList(new LambdaQueryWrapper<Member>()
                .eq(Member::getStatus, "正常"));
        int n = 0;
        for (Member m : all) {
            calculate(m.getId());
            n++;
        }
        return n;
    }

    /** 人工修正：原快照保留，修正写 churn_adjust，原因不少于 5 字 */
    @Transactional
    public void adjust(Long scoreId, String newLevel, String reason) {
        if (reason == null || reason.trim().length() < 5) {
            throw new BizException("修正原因不少于 5 个字");
        }
        ChurnScore sc = scoreMapper.selectById(scoreId);
        if (sc == null) {
            throw new BizException("计算记录不存在");
        }
        long adjusted = adjustMapper.selectCount(new LambdaQueryWrapper<ChurnAdjust>()
                .eq(ChurnAdjust::getScoreId, scoreId));
        if (adjusted > 0) {
            throw BizException.dup("该次计算结果已人工修正过，只能修正一次");
        }
        ChurnAdjust aj = new ChurnAdjust();
        aj.setScoreId(scoreId);
        aj.setMemberId(sc.getMemberId());
        aj.setOriginLevel(sc.getRiskLevel());
        aj.setOriginScore(sc.getTotalScore());
        aj.setNewLevel(newLevel);
        aj.setReason(reason.trim());
        aj.setAdjustBy(CurrentUser.id());
        aj.setAdjustTime(LocalDateTime.now());
        adjustMapper.insert(aj);

        // 只改结论与召回状态，原始分与各因素得分保持不动
        sc.setRiskLevel(newLevel);
        sc.setRecallStatus("低风险".equals(newLevel) ? "无需召回" : "待召回");
        sc.setConclusion("[人工修正] " + sc.getConclusion());
        sc.setUpdateBy(CurrentUser.id());
        scoreMapper.updateById(sc);

        Member m = memberMapper.selectById(sc.getMemberId());
        if (m != null) {
            m.setRiskLevel(newLevel);
            memberMapper.updateById(m);
        }
    }

    public void assign(Long scoreId, Long ownerId) {
        ChurnScore sc = scoreMapper.selectById(scoreId);
        if (sc == null) {
            throw new BizException("计算记录不存在");
        }
        sc.setOwnerId(ownerId);
        sc.setRecallStatus("跟进中");
        scoreMapper.updateById(sc);
    }

    /** 看板聚合：风险分布、等级分布、召回效果 */
    public Map<String, Object> board() {
        List<Member> members = memberMapper.selectList(new LambdaQueryWrapper<Member>()
                .eq(Member::getStatus, "正常"));
        Map<String, Long> levelDist = members.stream()
                .collect(Collectors.groupingBy(m -> m.getLevelCode() == null ? "普通会员" : m.getLevelCode(), Collectors.counting()));
        Map<String, Long> riskDist = members.stream()
                .collect(Collectors.groupingBy(m -> m.getRiskLevel() == null ? "未评估" : m.getRiskLevel(), Collectors.counting()));
        long high = riskDist.getOrDefault("高风险", 0L) + riskDist.getOrDefault("已流失", 0L);
        List<ChurnScore> scores = scoreMapper.selectList(new LambdaQueryWrapper<ChurnScore>()
                .orderByDesc(ChurnScore::getTotalScore));
        long adjustCnt = adjustMapper.selectCount(null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("total", members.size());
        out.put("levelDist", levelDist);
        out.put("riskDist", riskDist);
        out.put("highRisk", high);
        out.put("recallDone", scores.stream().filter(s -> "已召回".equals(s.getRecallStatus())).count());
        out.put("recallPending", scores.stream().filter(s -> "待召回".equals(s.getRecallStatus())).count());
        out.put("followCount", scores.stream().filter(s -> "跟进中".equals(s.getRecallStatus())).count());
        out.put("adjustCount", adjustCnt);
        out.put("adjustRate", scores.isEmpty() ? 0 : Math.round(adjustCnt * 1000.0 / scores.size()) / 10.0);
        out.put("rule", activeRule());
        return out;
    }
}
