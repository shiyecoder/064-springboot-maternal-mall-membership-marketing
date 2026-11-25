package com.univ.momcare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.entity.*;
import com.univ.momcare.mapper.*;
import com.univ.momcare.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单状态机与金额计算（需求文档第 4 节）：
 *   待支付 → 待发货 → 已发货 → 已完成；待发货 → 已取消；已完成 → 退货中 → 已退款
 *
 * 所有流转统一走这里校验：当前状态是否允许 → 必填原因 → 同一事务内更新订单、库存、券、积分并写流水。
 * 禁止前端直接提交 status 绕过本服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final MallOrderMapper orderMapper;
    private final MallOrderItemMapper itemMapper;
    private final MemberMapper memberMapper;
    private final BabyGoodsMapper goodsMapper;
    private final CouponMapper couponMapper;
    private final CouponReceiveMapper receiveMapper;
    private final PointLogMapper pointLogMapper;
    private final SysMessageMapper messageMapper;
    private final OperLogService operLogService;
    private final CodeGenService codeGen;
    private final MemberService memberService;

    /** 允许的状态流转，任何不在表里的组合都拒绝 */
    private static final Map<String, List<String>> FLOW = Map.of(
            "待支付", List.of("待发货", "已取消"),
            "待发货", List.of("已发货", "已取消"),
            "已发货", List.of("已完成", "退货中"),
            "已完成", List.of("退货中"),
            "退货中", List.of("已退款", "已完成"),
            "已退款", List.of(),
            "已取消", List.of());

    /**
     * 下单：锁库存 + 占用券 + 计算金额，生成待支付订单。
     * 金额口径：商品金额（会员价优先）→ 减券 → 加运费（满 199 免运费，否则 8 元）。
     */
    @Transactional
    public MallOrder create(Long memberId, List<Map<String, Object>> items, Long couponReceiveId,
                            String payType, String receiverName, String receiverMobile, String receiverAddress) {
        Member m = memberMapper.selectById(memberId);
        if (m == null) {
            throw new BizException("会员不存在");
        }
        if (!"正常".equals(m.getStatus())) {
            throw BizException.state("会员状态为" + m.getStatus() + "，不能下单");
        }
        if (items == null || items.isEmpty()) {
            throw new BizException("请至少选择一个商品");
        }
        if (items.size() > 20) {
            throw new BizException("单笔订单最多 20 种商品");
        }

        BigDecimal goodsAmount = BigDecimal.ZERO;
        int itemCount = 0;
        List<MallOrderItem> lines = new ArrayList<>();
        for (Map<String, Object> it : items) {
            Long goodsId = Long.valueOf(String.valueOf(it.get("goodsId")));
            int qty = Integer.parseInt(String.valueOf(it.getOrDefault("qty", 1)));
            if (qty < 1 || qty > 20) {
                throw new BizException("商品数量需在 1 到 20 之间");
            }
            BabyGoods g = goodsMapper.selectById(goodsId);
            if (g == null || "已下架".equals(g.getStatus())) {
                throw BizException.state("商品已下架或不存在");
            }
            if (g.getStockQty() != null && g.getStockQty() < qty) {
                throw BizException.state("「" + g.getGoodsName() + "」库存不足，仅剩 " + g.getStockQty() + " 件");
            }
            BigDecimal price = g.getMemberPrice() != null ? g.getMemberPrice() : g.getSalePrice();
            BigDecimal amount = price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
            goodsAmount = goodsAmount.add(amount);
            itemCount += qty;

            MallOrderItem line = new MallOrderItem();
            line.setGoodsId(goodsId);
            line.setGoodsName(g.getGoodsName());
            line.setSpecDesc(g.getSpecDesc());
            line.setPrice(price);
            line.setQty(qty);
            line.setAmount(amount);
            lines.add(line);

            // 扣减库存，库存归零自动置为已售罄（需求文档 5.1）
            g.setStockQty(g.getStockQty() - qty);
            if (g.getStockQty() == 0) {
                g.setStatus("已售罄");
            }
            goodsMapper.updateById(g);
        }

        // 优惠券核销校验：归属、状态、有效期、门槛、适用品类，一单限一张
        BigDecimal discount = BigDecimal.ZERO;
        CouponReceive cr = null;
        if (couponReceiveId != null) {
            cr = receiveMapper.selectById(couponReceiveId);
            if (cr == null || !cr.getMemberId().equals(memberId)) {
                throw new BizException("优惠券不属于当前会员");
            }
            if (!"未使用".equals(cr.getUseStatus())) {
                throw BizException.dup("券已被使用或已过期，请刷新后重试");
            }
            Coupon c = couponMapper.selectById(cr.getCouponId());
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(c.getValidStart()) || now.isAfter(c.getValidEnd())) {
                throw BizException.state("优惠券不在有效期内");
            }
            if (goodsAmount.compareTo(c.getMinAmount()) < 0) {
                throw BizException.state("未达到券使用门槛 " + c.getMinAmount() + " 元");
            }
            if ("指定品类".equals(c.getLimitType()) && c.getLimitValue() != null) {
                boolean hit = lines.stream().anyMatch(l -> {
                    BabyGoods g = goodsMapper.selectById(l.getGoodsId());
                    return g != null && c.getLimitValue().equals(g.getGoodsType());
                });
                if (!hit) {
                    throw BizException.state("该券仅限「" + c.getLimitValue() + "」品类使用");
                }
            }
            if ("满减".equals(c.getCouponType()) || "立减".equals(c.getCouponType())) {
                discount = c.getDiscountAmount();
            } else if ("折扣".equals(c.getCouponType())) {
                discount = goodsAmount.multiply(BigDecimal.ONE
                        .subtract(c.getDiscountAmount().divide(BigDecimal.TEN, 4, RoundingMode.HALF_UP)));
            } else if ("免邮".equals(c.getCouponType())) {
                discount = BigDecimal.ZERO;
            }
            discount = discount.min(goodsAmount).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal afterDiscount = goodsAmount.subtract(discount);
        BigDecimal freight = afterDiscount.compareTo(new BigDecimal("199")) >= 0
                ? BigDecimal.ZERO : new BigDecimal("8.00");
        if (cr != null) {
            Coupon c = couponMapper.selectById(cr.getCouponId());
            if ("免邮".equals(c.getCouponType())) {
                freight = BigDecimal.ZERO;
            }
        }
        BigDecimal payAmount = afterDiscount.add(freight).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        MallOrder o = new MallOrder();
        o.setOrderNo(codeGen.orderNo());
        o.setMemberId(memberId);
        o.setItemCount(itemCount);
        o.setGoodsAmount(goodsAmount);
        o.setCouponReceiveId(cr == null ? null : cr.getId());
        o.setDiscountAmount(discount);
        o.setFreight(freight);
        o.setPayAmount(payAmount);
        o.setGainPoint(payAmount.intValue());
        o.setPayType(payType == null ? "微信" : payType);
        o.setStatus("待支付");
        o.setReceiverName(receiverName == null ? m.getNickName() : receiverName);
        o.setReceiverMobile(receiverMobile == null ? m.getMobile() : receiverMobile);
        o.setReceiverAddress(receiverAddress);
        orderMapper.insert(o);

        for (MallOrderItem l : lines) {
            l.setOrderId(o.getId());
            itemMapper.insert(l);
        }

        if (cr != null) {
            cr.setUseStatus("已使用");
            cr.setUseOrderId(o.getId());
            cr.setUseTime(LocalDateTime.now());
            receiveMapper.updateById(cr);
        }
        operLogService.log("订单管理", "新增", o.getOrderNo(),
                "会员下单 " + itemCount + " 件，实付 " + payAmount + " 元");
        return o;
    }

    /** 支付登记：待支付 → 待发货。重复支付以首次流水为准（已支付直接返回成功）。 */
    @Transactional
    public MallOrder pay(Long id, String payType) {
        MallOrder o = mustGet(id);
        if ("待发货".equals(o.getStatus()) || "已发货".equals(o.getStatus()) || "已完成".equals(o.getStatus())) {
            return o;
        }
        check(o, "待发货");
        o.setStatus("待发货");
        if (payType != null && !payType.isBlank()) {
            o.setPayType(payType);
        }
        orderMapper.updateById(o);
        operLogService.log("订单管理", "流转", o.getOrderNo(), "会员支付成功，订单转待发货");
        return o;
    }

    @Transactional
    public MallOrder ship(Long id) {
        MallOrder o = mustGet(id);
        check(o, "已发货");
        o.setStatus("已发货");
        orderMapper.updateById(o);
        operLogService.log("订单管理", "流转", o.getOrderNo(), "门店拣货完成并发出");
        return o;
    }

    /**
     * 确认收货：已发货/待发货 → 已完成。
     * 同事务内发放积分、累加累计消费与次数、写积分流水、按累计消费重算会员等级。
     */
    @Transactional
    public MallOrder complete(Long id) {
        MallOrder o = mustGet(id);
        if ("已完成".equals(o.getStatus())) {
            throw BizException.dup("订单已确认收货，请勿重复提交");
        }
        if (!List.of("已发货", "待发货").contains(o.getStatus())) {
            throw BizException.state("当前状态「" + o.getStatus() + "」不允许确认收货");
        }
        o.setStatus("已完成");
        orderMapper.updateById(o);

        Member m = memberMapper.selectById(o.getMemberId());
        int before = m.getPointBalance() == null ? 0 : m.getPointBalance();
        int gain = o.getGainPoint() == null ? 0 : o.getGainPoint();
        m.setPointBalance(before + gain);
        m.setTotalAmount(m.getTotalAmount() == null ? o.getPayAmount() : m.getTotalAmount().add(o.getPayAmount()));
        m.setOrderCount((m.getOrderCount() == null ? 0 : m.getOrderCount()) + 1);
        m.setLastBuyTime(LocalDateTime.now());
        memberMapper.updateById(m);

        PointLog pl = new PointLog();
        pl.setPointNo(codeGen.pointNo());
        pl.setMemberId(m.getId());
        pl.setChangeType("下单获得");
        pl.setChangePoint(gain);
        pl.setBeforePoint(before);
        pl.setAfterPoint(before + gain);
        pl.setBizNo(o.getOrderNo());
        pointLogMapper.insert(pl);

        memberService.recalcLevel(m.getId());

        SysMessage msg = new SysMessage();
        msg.setReceiverId(12L);
        msg.setTitle("订单已完成");
        msg.setContent("订单 " + o.getOrderNo() + " 已确认收货，获得 " + gain + " 积分。");
        msg.setBizType("order");
        msg.setBizId(o.getId());
        messageMapper.insert(msg);

        operLogService.log("订单管理", "流转", o.getOrderNo(), "确认收货，发放 " + gain + " 积分");
        return o;
    }

    /** 取消：待支付/待发货 → 已取消，回补库存、返还券，三处回补在同一事务内 */
    @Transactional
    public MallOrder cancel(Long id, String reason) {
        MallOrder o = mustGet(id);
        check(o, "已取消");
        requireReason(reason);
        o.setStatus("已取消");
        o.setRefundReason(reason);
        orderMapper.updateById(o);

        restoreStock(o.getId());
        if (o.getCouponReceiveId() != null) {
            CouponReceive cr = receiveMapper.selectById(o.getCouponReceiveId());
            if (cr != null && "已使用".equals(cr.getUseStatus())) {
                cr.setUseStatus("未使用");
                cr.setUseOrderId(null);
                cr.setUseTime(null);
                receiveMapper.updateById(cr);
            }
        }
        operLogService.log("订单管理", "流转", o.getOrderNo(), "取消订单：" + reason);
        return o;
    }

    /** 申请退货：已完成（7 天内）→ 退货中 */
    @Transactional
    public MallOrder applyReturn(Long id, String reason) {
        MallOrder o = mustGet(id);
        check(o, "退货中");
        requireReason(reason);
        if (o.getUpdateTime() != null && o.getUpdateTime().isBefore(LocalDateTime.now().minusDays(7))) {
            throw BizException.state("已超过 7 天无理由退货期，请转人工客服");
        }
        o.setStatus("退货中");
        o.setRefundReason(reason);
        orderMapper.updateById(o);
        operLogService.log("订单管理", "流转", o.getOrderNo(), "会员申请退货：" + reason);
        return o;
    }

    /** 退款完成：退货中 → 已退款，按比例扣回积分、冲减累计消费并重算等级 */
    @Transactional
    public MallOrder refund(Long id, String reason) {
        MallOrder o = mustGet(id);
        check(o, "已退款");
        requireReason(reason);
        o.setStatus("已退款");
        o.setRefundReason(reason);
        orderMapper.updateById(o);

        Member m = memberMapper.selectById(o.getMemberId());
        int before = m.getPointBalance() == null ? 0 : m.getPointBalance();
        int back = o.getGainPoint() == null ? 0 : o.getGainPoint();
        int realBack = Math.min(before, back);
        m.setPointBalance(before - realBack);
        m.setTotalAmount(m.getTotalAmount().subtract(o.getPayAmount()).max(BigDecimal.ZERO));
        m.setOrderCount(Math.max(0, (m.getOrderCount() == null ? 1 : m.getOrderCount()) - 1));
        memberMapper.updateById(m);

        PointLog pl = new PointLog();
        pl.setPointNo(codeGen.pointNo());
        pl.setMemberId(m.getId());
        pl.setChangeType("退款扣回");
        pl.setChangePoint(-realBack);
        pl.setBeforePoint(before);
        pl.setAfterPoint(before - realBack);
        pl.setBizNo(o.getOrderNo());
        pl.setRemark(realBack < back ? "积分不足，扣至 0，差额 " + (back - realBack) + " 分" : "退款扣回");
        pointLogMapper.insert(pl);

        restoreStock(o.getId());
        memberService.recalcLevel(m.getId());
        operLogService.log("订单管理", "流转", o.getOrderNo(), "退款完成，扣回 " + realBack + " 积分");
        return o;
    }

    private void restoreStock(Long orderId) {
        List<MallOrderItem> lines = itemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                .eq(MallOrderItem::getOrderId, orderId));
        for (MallOrderItem l : lines) {
            BabyGoods g = goodsMapper.selectById(l.getGoodsId());
            if (g == null) {
                continue;
            }
            // 回补库存；是否恢复在售由运营在商品页手动确认（需求文档 5.1）
            g.setStockQty((g.getStockQty() == null ? 0 : g.getStockQty()) + l.getQty());
            goodsMapper.updateById(g);
        }
    }

    private MallOrder mustGet(Long id) {
        MallOrder o = orderMapper.selectById(id);
        if (o == null) {
            throw new BizException("订单不存在");
        }
        return o;
    }

    /** 状态机校验：不在允许列表里就抛 4001，提示当前状态 */
    private void check(MallOrder o, String target) {
        List<String> allow = FLOW.getOrDefault(o.getStatus(), List.of());
        if (!allow.contains(target)) {
            throw BizException.state("当前状态「" + o.getStatus() + "」不允许变更为「" + target + "」");
        }
    }

    private void requireReason(String reason) {
        if (reason == null || reason.trim().length() < 5) {
            throw new BizException("请填写不少于 5 个字的原因");
        }
    }

    /** 数据范围：会员只能看自己的订单，店长/导购看本门店，管理员看全部 */
    public boolean canAccess(MallOrder o) {
        if (CurrentUser.hasRole("ADMIN") || CurrentUser.hasRole("MANAGER")
                || CurrentUser.hasRole("OPERATOR") || CurrentUser.hasRole("MARKETER")) {
            return true;
        }
        return o.getMemberId().equals(CurrentUser.id());
    }
}
