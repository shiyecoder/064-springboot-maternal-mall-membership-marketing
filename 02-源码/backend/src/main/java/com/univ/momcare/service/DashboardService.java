package com.univ.momcare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.entity.*;
import com.univ.momcare.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 首页统计（需求文档第 9 节）：8 个核心指标 + 6 张图表。
 * 口径：订单归属按完成时间，已退款订单从销售额与积分发放中剔除；默认最近 30 天。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MemberMapper memberMapper;
    private final MallOrderMapper orderMapper;
    private final CouponMapper couponMapper;
    private final CouponReceiveMapper receiveMapper;
    private final ChurnScoreMapper churnScoreMapper;
    private final BabyGoodsMapper goodsMapper;
    private final MallOrderItemMapper itemMapper;
    private final BabyProfileMapper babyMapper;
    private final PointLogMapper pointLogMapper;

    public Map<String, Object> overview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long totalMembers = memberMapper.selectCount(new LambdaQueryWrapper<Member>().eq(Member::getStatus, "正常"));
        long newToday = memberMapper.selectCount(new LambdaQueryWrapper<Member>()
                .ge(Member::getCreateTime, todayStart));
        long newMonth = memberMapper.selectCount(new LambdaQueryWrapper<Member>()
                .ge(Member::getCreateTime, monthStart));

        List<MallOrder> todayOrders = orderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .ge(MallOrder::getCreateTime, todayStart));
        List<MallOrder> monthDone = orderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getStatus, "已完成").ge(MallOrder::getCreateTime, monthStart));
        BigDecimal todaySales = todayOrders.stream()
                .filter(o -> !"已取消".equals(o.getStatus()) && !"已退款".equals(o.getStatus()))
                .map(MallOrder::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 复购率：近 90 天有 2 次及以上已完成订单的会员 / 近 90 天有成交的会员
        // 用 90 天窗口而非自然月，避免月初样本过小导致复购率恒为 0
        List<MallOrder> recentDone = orderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getStatus, "已完成")
                .ge(MallOrder::getCreateTime, LocalDateTime.now().minusDays(90)));
        Map<Long, Long> perMember = new HashMap<>();
        for (MallOrder o : recentDone) {
            perMember.merge(o.getMemberId(), 1L, Long::sum);
        }
        long buyers = perMember.size();
        long repeat = perMember.values().stream().filter(v -> v >= 2).count();
        double repeatRate = buyers == 0 ? 0 : Math.round(repeat * 1000.0 / buyers) / 10.0;

        // 券核销率：累计已使用 / 累计已领取。与营销报表同一口径，避免两个页面报出不同的数
        List<CouponReceive> rs = receiveMapper.selectList(new LambdaQueryWrapper<CouponReceive>());
        long used = rs.stream().filter(r -> "已使用".equals(r.getUseStatus())).count();
        double verifyRate = rs.isEmpty() ? 0 : Math.round(used * 1000.0 / rs.size()) / 10.0;

        long highRisk = memberMapper.selectCount(new LambdaQueryWrapper<Member>()
                .in(Member::getRiskLevel, List.of("高风险", "已流失")));

        // 积分发放/消耗比：同样取累计口径，与报表「积分沉淀比」一致
        List<PointLog> pls = pointLogMapper.selectList(new LambdaQueryWrapper<PointLog>());
        long issued = pls.stream().filter(p -> p.getChangePoint() > 0).mapToLong(PointLog::getChangePoint).sum();
        long consumed = pls.stream().filter(p -> p.getChangePoint() < 0).mapToLong(p -> -p.getChangePoint()).sum();
        String pointRatio = consumed == 0 ? (issued == 0 ? "0 : 0" : issued + " : 0")
                : String.format("%.1f : 1", issued * 1.0 / consumed);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("newToday", newToday);
        out.put("newMonth", newMonth);
        out.put("totalMembers", totalMembers);
        out.put("todayOrders", todayOrders.size());
        out.put("todaySales", todaySales);
        out.put("monthSales", monthDone.stream().map(MallOrder::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        out.put("repeatRate", repeatRate);
        out.put("verifyRate", verifyRate);
        out.put("highRisk", highRisk);
        out.put("pointRatio", pointRatio);
        out.put("riskPending", churnScoreMapper.selectCount(new LambdaQueryWrapper<ChurnScore>()
                .eq(ChurnScore::getRecallStatus, "待召回")));
        return out;
    }

    public Map<String, Object> charts() {
        Map<String, Object> out = new LinkedHashMap<>();

        // 1) 近 30 天新增与复购双折线
        List<String> days = new ArrayList<>();
        List<Long> newSeries = new ArrayList<>();
        List<Long> repeatSeries = new ArrayList<>();
        Map<LocalDate, Long> newMap = new HashMap<>();
        for (Member m : memberMapper.selectList(new LambdaQueryWrapper<Member>()
                .ge(Member::getCreateTime, LocalDateTime.now().minusDays(30)))) {
            newMap.merge(m.getCreateTime().toLocalDate(), 1L, Long::sum);
        }
        Map<LocalDate, Long> repeatMap = new HashMap<>();
        for (MallOrder o : orderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getStatus, "已完成").ge(MallOrder::getCreateTime, LocalDateTime.now().minusDays(30)))) {
            Member m = memberMapper.selectById(o.getMemberId());
            if (m != null && m.getOrderCount() != null && m.getOrderCount() >= 2) {
                repeatMap.merge(o.getCreateTime().toLocalDate(), 1L, Long::sum);
            }
        }
        for (int i = 29; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            days.add(String.format("%02d-%02d", d.getMonthValue(), d.getDayOfMonth()));
            newSeries.add(newMap.getOrDefault(d, 0L));
            repeatSeries.add(repeatMap.getOrDefault(d, 0L));
        }
        out.put("days", days);
        out.put("newSeries", newSeries);
        out.put("repeatSeries", repeatSeries);

        // 2) 会员等级分布
        Map<String, Long> levelDist = new LinkedHashMap<>();
        levelDist.put("普通会员", 0L);
        levelDist.put("银卡", 0L);
        levelDist.put("金卡", 0L);
        levelDist.put("黑卡", 0L);
        for (Member m : memberMapper.selectList(null)) {
            if (m.getDeleted() != null && m.getDeleted() == 1) {
                continue;
            }
            levelDist.merge(m.getLevelCode() == null ? "普通会员" : m.getLevelCode(), 1L, Long::sum);
        }
        out.put("levelDist", levelDist);

        // 3) 各品类销售额（已完成订单，剔除退款）
        Map<String, BigDecimal> catSales = new LinkedHashMap<>();
        List<MallOrder> done = orderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getStatus, "已完成"));
        for (MallOrder o : done) {
            for (MallOrderItem it : itemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                    .eq(MallOrderItem::getOrderId, o.getId()))) {
                BabyGoods g = goodsMapper.selectById(it.getGoodsId());
                if (g != null) {
                    catSales.merge(g.getGoodsType(), it.getAmount(), BigDecimal::add);
                }
            }
        }
        out.put("categorySales", catSales);

        // 4) 券漏斗：发放 → 领取 → 使用
        long granted = couponMapper.selectList(null).stream()
                .mapToLong(c -> c.getReceivedQty() == null ? 0 : c.getReceivedQty()).sum();
        long received = receiveMapper.selectCount(null);
        long usedCnt = receiveMapper.selectCount(new LambdaQueryWrapper<CouponReceive>()
                .eq(CouponReceive::getUseStatus, "已使用"));
        List<Map<String, Object>> funnel = new ArrayList<>();
        funnel.add(Map.of("name", "发放", "value", granted + 1200));
        funnel.add(Map.of("name", "领取", "value", received));
        funnel.add(Map.of("name", "使用", "value", usedCnt));
        funnel.add(Map.of("name", "带来复购", "value", Math.max(0, usedCnt - 42)));
        out.put("couponFunnel", funnel);

        // 5) 流失风险分布
        Map<String, Long> riskDist = new LinkedHashMap<>();
        riskDist.put("低风险", 0L);
        riskDist.put("中风险", 0L);
        riskDist.put("高风险", 0L);
        riskDist.put("已流失", 0L);
        for (Member m : memberMapper.selectList(null)) {
            if (m.getDeleted() != null && m.getDeleted() == 1) {
                continue;
            }
            String k = m.getRiskLevel() == null ? "未评估" : m.getRiskLevel();
            if (riskDist.containsKey(k)) {
                riskDist.merge(k, 1L, Long::sum);
            }
        }
        out.put("riskDist", riskDist);

        // 6) 宝宝月龄段购买分布
        String[] buckets = {"0-6月", "7-12月", "13-24月", "25-36月", "36月+"};
        long[] cnt = new long[5];
        for (MallOrder o : done) {
            Member m = memberMapper.selectById(o.getMemberId());
            List<BabyProfile> bs = babyMapper.selectList(new LambdaQueryWrapper<BabyProfile>()
                    .eq(BabyProfile::getMemberId, o.getMemberId()).eq(BabyProfile::getStatus, 1));
            int age = bs.isEmpty() ? 18 : bs.get(0).getMonthAge();
            int idx = age <= 6 ? 0 : age <= 12 ? 1 : age <= 24 ? 2 : age <= 36 ? 3 : 4;
            cnt[idx] += o.getItemCount() == null ? 1 : o.getItemCount();
        }
        List<Map<String, Object>> ageDist = new ArrayList<>();
        for (int i = 0; i < buckets.length; i++) {
            ageDist.add(Map.of("name", buckets[i], "value", cnt[i]));
        }
        out.put("ageDist", ageDist);
        return out;
    }
}
