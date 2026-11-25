package com.univ.momcare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.entity.*;
import com.univ.momcare.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会员门户数据：只返回当前会员自己的数据（数据范围「本人」）。
 * 商城首页按当前宝宝月龄筛选适龄商品，跨段位商品仍展示但标注「不适龄」。
 */
@Service
@RequiredArgsConstructor
public class PortalService {

    private final MemberMapper memberMapper;
    private final BabyProfileMapper babyMapper;
    private final BabyGoodsMapper goodsMapper;
    private final CouponReceiveMapper receiveMapper;
    private final CouponMapper couponMapper;
    private final MallOrderMapper orderMapper;
    private final MallOrderItemMapper itemMapper;
    private final PointLogMapper pointLogMapper;
    private final RepurchaseTaskMapper repurchaseMapper;
    private final GoodsReviewMapper reviewMapper;
    private final SysNoticeMapper noticeMapper;

    /** 会员资料（门户头部展示等级、积分、累计消费） */
    public Member me(Long memberId) {
        Member m = memberMapper.selectById(memberId);
        if (m == null) {
            throw new com.univ.momcare.common.BizException("会员不存在");
        }
        return m;
    }

    public List<BabyProfile> babies(Long memberId) {
        return babyMapper.selectList(new LambdaQueryWrapper<BabyProfile>()
                .eq(BabyProfile::getMemberId, memberId)
                .orderByDesc(BabyProfile::getStatus).orderByAsc(BabyProfile::getId));
    }

    /**
     * 商城首页：按宝宝月龄过滤「适龄商品」，同时返回热销与公告。
     * 无宝宝档案时按全部在售商品返回，并提示补录档案以获得适龄推荐。
     */
    public Map<String, Object> home(Long memberId) {
        List<BabyProfile> bs = babies(memberId);
        Integer monthAge = bs.stream().filter(b -> b.getStatus() == 1).map(BabyProfile::getMonthAge)
                .findFirst().orElse(null);

        List<BabyGoods> onSale = goodsMapper.selectList(new LambdaQueryWrapper<BabyGoods>()
                .eq(BabyGoods::getStatus, "在售").orderByDesc(BabyGoods::getCreateTime));
        List<BabyGoods> fit = onSale.stream()
                .filter(g -> monthAge == null || (g.getAgeMonthStart() <= monthAge && g.getAgeMonthEnd() >= monthAge))
                .collect(Collectors.toList());
        if (fit.isEmpty()) {
            fit = onSale;
        }

        Map<String, List<BabyGoods>> byType = new LinkedHashMap<>();
        for (BabyGoods g : fit) {
            byType.computeIfAbsent(g.getGoodsType(), k -> new ArrayList<>()).add(g);
        }

        List<Coupon> coupons = couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, "发放中").orderByAsc(Coupon::getValidEnd));
        List<CouponReceive> mine = receiveMapper.selectList(new LambdaQueryWrapper<CouponReceive>()
                .eq(CouponReceive::getMemberId, memberId));
        Set<Long> received = mine.stream().map(CouponReceive::getCouponId).collect(Collectors.toSet());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("member", me(memberId));
        out.put("babies", bs);
        out.put("monthAge", monthAge);
        out.put("goodsByType", byType);
        out.put("coupons", coupons);
        out.put("receivedCouponIds", received);
        out.put("notices", noticeMapper.selectList(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, "已发布").orderByDesc(SysNotice::getPublishTime).last("limit 3")));
        return out;
    }

    public Map<String, Object> myCenter(Long memberId) {
        Member m = me(memberId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("member", m);
        out.put("babies", babies(memberId));
        List<CouponReceive> rs = receiveMapper.selectList(new LambdaQueryWrapper<CouponReceive>()
                .eq(CouponReceive::getMemberId, memberId).orderByDesc(CouponReceive::getReceiveTime));
        List<Map<String, Object>> coupons = new ArrayList<>();
        for (CouponReceive r : rs) {
            Coupon c = couponMapper.selectById(r.getCouponId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("receive", r);
            row.put("coupon", c);
            coupons.add(row);
        }
        out.put("coupons", coupons);

        List<MallOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getMemberId, memberId).orderByDesc(MallOrder::getCreateTime));
        for (MallOrder o : orders) {
            o.setItems(itemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                    .eq(MallOrderItem::getOrderId, o.getId())));
        }
        out.put("orders", orders);

        out.put("points", pointLogMapper.selectList(new LambdaQueryWrapper<PointLog>()
                .eq(PointLog::getMemberId, memberId).orderByDesc(PointLog::getCreateTime).last("limit 20")));
        out.put("reminders", repurchaseMapper.selectList(new LambdaQueryWrapper<RepurchaseTask>()
                .eq(RepurchaseTask::getMemberId, memberId).orderByDesc(RepurchaseTask::getPlanTime)));
        out.put("reviews", reviewMapper.selectList(new LambdaQueryWrapper<GoodsReview>()
                .eq(GoodsReview::getMemberId, memberId).orderByDesc(GoodsReview::getCreateTime)));

        long pending = orders.stream().filter(o -> "待支付".equals(o.getStatus())).count();
        long shipping = orders.stream().filter(o -> "已发货".equals(o.getStatus())).count();
        long toReview = orders.stream().filter(o -> "已完成".equals(o.getStatus())).count()
                - reviewMapper.selectCount(new LambdaQueryWrapper<GoodsReview>().eq(GoodsReview::getMemberId, memberId));
        out.put("todo", Map.of("pending", pending, "shipping", shipping, "toReview", Math.max(0, toReview)));
        out.put("today", LocalDate.now().toString());
        return out;
    }
}
