package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.MallOrder;
import com.univ.momcare.entity.MallOrderItem;
import com.univ.momcare.mapper.MallOrderItemMapper;
import com.univ.momcare.mapper.MallOrderMapper;
import com.univ.momcare.security.CurrentUser;
import com.univ.momcare.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商城订单：查询 + 状态流转。
 * 所有状态变更都走 OrderService 的状态机校验，本类不直接改 status。
 */
@RestController
@RequestMapping("/api/mall-order")
@RequiredArgsConstructor
public class OrderController extends BaseCrudController<MallOrder> {

    private final MallOrderMapper orderMapper;
    private final MallOrderItemMapper itemMapper;
    private final OrderService orderService;

    @Override
    protected BaseMapper<MallOrder> mapper() {
        return orderMapper;
    }

    @Override
    protected Class<MallOrder> clazz() {
        return MallOrder.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"order_no", "receiver_name", "receiver_mobile"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"status", "member_id", "pay_type"};
    }

    /** 详情带订单明细 */
    @Override
    @GetMapping("/{id}")
    public R<MallOrder> detail(@PathVariable Long id) {
        MallOrder o = orderMapper.selectById(id);
        if (o == null) {
            throw new BizException("订单不存在");
        }
        o.setItems(itemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                .eq(MallOrderItem::getOrderId, id)));
        nameFill.fill(List.of(o));
        return R.ok(o);
    }

    /** 下单：商品清单 + 可选券，金额与库存由后端计算 */
    @PostMapping("/create")
    public R<MallOrder> create(@RequestBody Map<String, Object> body) {
        Long memberId = Long.valueOf(String.valueOf(body.get("memberId")));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        Object cid = body.get("couponReceiveId");
        Long couponReceiveId = cid == null || "".equals(cid) ? null : Long.valueOf(String.valueOf(cid));
        String address = body.get("receiverAddress") == null ? null : String.valueOf(body.get("receiverAddress"));
        return R.ok(orderService.create(memberId, items, couponReceiveId,
                (String) body.get("payType"), (String) body.get("receiverName"),
                (String) body.get("receiverMobile"), address));
    }

    @PostMapping("/{id}/pay")
    public R<MallOrder> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return R.ok(orderService.pay(id, body == null ? null : body.get("payType")));
    }

    @PostMapping("/{id}/ship")
    public R<MallOrder> ship(@PathVariable Long id) {
        return R.ok(orderService.ship(id));
    }

    @PostMapping("/{id}/complete")
    public R<MallOrder> complete(@PathVariable Long id) {
        return R.ok(orderService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    public R<MallOrder> cancel(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(orderService.cancel(id, body.get("reason")));
    }

    @PostMapping("/{id}/apply-return")
    public R<MallOrder> applyReturn(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(orderService.applyReturn(id, body.get("reason")));
    }

    @PostMapping("/{id}/refund")
    public R<MallOrder> refund(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(orderService.refund(id, body.get("reason")));
    }

    /** 我的订单（会员门户用，只返回本人） */
    @GetMapping("/mine")
    public R<List<MallOrder>> mine() {
        List<MallOrder> list = orderMapper.selectList(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getMemberId, CurrentUser.id()).orderByDesc(MallOrder::getCreateTime));
        for (MallOrder o : list) {
            o.setItems(itemMapper.selectList(new LambdaQueryWrapper<MallOrderItem>()
                    .eq(MallOrderItem::getOrderId, o.getId())));
        }
        return R.ok(list);
    }
}
