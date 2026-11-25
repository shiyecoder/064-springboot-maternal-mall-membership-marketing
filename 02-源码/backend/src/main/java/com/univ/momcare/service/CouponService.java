package com.univ.momcare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.entity.Coupon;
import com.univ.momcare.entity.CouponReceive;
import com.univ.momcare.entity.Member;
import com.univ.momcare.mapper.CouponMapper;
import com.univ.momcare.mapper.CouponReceiveMapper;
import com.univ.momcare.mapper.MemberMapper;
import com.univ.momcare.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 优惠券发放与核销（需求文档 5.3）。
 * 定向发放按会员等级与流失风险筛选，同一会员同一券活动最多 1 张，
 * 已领数量达到发放总量即停止，未使用券到期由定时任务置为已过期且不返还总量。
 */
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapper couponMapper;
    private final CouponReceiveMapper receiveMapper;
    private final MemberMapper memberMapper;
    private final CodeGenService codeGen;
    private final OperLogService operLogService;

    /**
     * 定向发券：按等级 + 风险 + 最近购买间隔筛人，跳过已领过该券的会员。
     * 返回实际发放张数、跳过数与券成本，便于前端展示预算占用。
     */
    @Transactional
    public Map<String, Object> grant(Long couponId, List<String> levels, List<String> risks, Integer minGapDays, Integer limit) {
        Coupon c = couponMapper.selectById(couponId);
        if (c == null) {
            throw new BizException("券活动不存在");
        }
        if (!"发放中".equals(c.getStatus())) {
            throw BizException.state("券活动状态为「" + c.getStatus() + "」，不能发放");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(c.getValidEnd())) {
            throw BizException.state("券活动已过期");
        }

        List<Member> all = memberMapper.selectList(new LambdaQueryWrapper<Member>()
                .eq(Member::getStatus, "正常"));
        List<Member> targets = new ArrayList<>();
        for (Member m : all) {
            if (levels != null && !levels.isEmpty() && !levels.contains(m.getLevelCode())) {
                continue;
            }
            if (risks != null && !risks.isEmpty() && !risks.contains(m.getRiskLevel())) {
                continue;
            }
            if (minGapDays != null && minGapDays > 0) {
                if (m.getLastBuyTime() == null) {
                    continue;
                }
                long gap = java.time.temporal.ChronoUnit.DAYS.between(m.getLastBuyTime(), now);
                if (gap < minGapDays) {
                    continue;
                }
            }
            targets.add(m);
        }

        int remain = c.getTotalQty() - c.getReceivedQty();
        if (remain <= 0) {
            throw BizException.state("券已发放完毕");
        }
        int cap = limit == null ? remain : Math.min(remain, limit);
        int granted = 0;
        int skipped = 0;
        for (Member m : targets) {
            if (granted >= cap) {
                break;
            }
            long exists = receiveMapper.selectCount(new LambdaQueryWrapper<CouponReceive>()
                    .eq(CouponReceive::getCouponId, couponId).eq(CouponReceive::getMemberId, m.getId()));
            if (exists > 0) {
                skipped++;
                continue;
            }
            CouponReceive cr = new CouponReceive();
            cr.setReceiveNo(codeGen.receiveNo());
            cr.setCouponId(couponId);
            cr.setMemberId(m.getId());
            cr.setReceiveChannel("导购发放");
            cr.setReceiveTime(now);
            cr.setUseStatus("未使用");
            cr.setGrantBy(CurrentUser.id());
            receiveMapper.insert(cr);
            granted++;
        }
        c.setReceivedQty(c.getReceivedQty() + granted);
        if (c.getReceivedQty() >= c.getTotalQty()) {
            c.setStatus("已停发");
        }
        couponMapper.updateById(c);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("granted", granted);
        out.put("skipped", skipped);
        out.put("targets", targets.size());
        out.put("cost", granted * c.getDiscountAmount().doubleValue());
        out.put("remainQty", c.getTotalQty() - c.getReceivedQty());
        operLogService.log("优惠券配置", "发券", c.getCouponNo(),
                "定向发放 " + granted + " 张，跳过已领 " + skipped + " 人");
        return out;
    }

    /** 会员自助领券：同一券活动限领 1 张 */
    @Transactional
    public CouponReceive receive(Long couponId, Long memberId) {
        Coupon c = couponMapper.selectById(couponId);
        if (c == null) {
            throw new BizException("券活动不存在");
        }
        if (!"发放中".equals(c.getStatus())) {
            throw BizException.state("该券当前不可领取");
        }
        CouponReceive exist = receiveMapper.selectOne(new LambdaQueryWrapper<CouponReceive>()
                .eq(CouponReceive::getCouponId, couponId).eq(CouponReceive::getMemberId, memberId).last("limit 1"));
        if (exist != null) {
            return exist;  // 幂等：重复领取直接返回已领取记录，不重复占用总量
        }
        if (c.getReceivedQty() >= c.getTotalQty()) {
            throw BizException.state("券已领完");
        }
        CouponReceive cr = new CouponReceive();
        cr.setReceiveNo(codeGen.receiveNo());
        cr.setCouponId(couponId);
        cr.setMemberId(memberId);
        cr.setReceiveChannel("商城领取");
        cr.setReceiveTime(LocalDateTime.now());
        cr.setUseStatus("未使用");
        receiveMapper.insert(cr);

        c.setReceivedQty(c.getReceivedQty() + 1);
        couponMapper.updateById(c);
        return cr;
    }

    /** 到期券置为已过期：不返还发放总量 */
    @Transactional
    public int expireAll() {
        List<Coupon> list = couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .lt(Coupon::getValidEnd, LocalDateTime.now()));
        for (Coupon c : list) {
            if (!"已过期".equals(c.getStatus())) {
                c.setStatus("已过期");
                couponMapper.updateById(c);
            }
            List<CouponReceive> rs = receiveMapper.selectList(new LambdaQueryWrapper<CouponReceive>()
                    .eq(CouponReceive::getCouponId, c.getId()).eq(CouponReceive::getUseStatus, "未使用"));
            for (CouponReceive r : rs) {
                r.setUseStatus("已过期");
                receiveMapper.updateById(r);
            }
        }
        return list.size();
    }
}
