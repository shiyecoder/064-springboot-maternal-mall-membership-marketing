package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.CouponReceive;
import com.univ.momcare.mapper.CouponReceiveMapper;
import com.univ.momcare.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 领券记录：分页查询 + 会员自助领券 */
@RestController
@RequestMapping("/api/coupon-receive")
@RequiredArgsConstructor
public class CouponReceiveController extends BaseCrudController<CouponReceive> {

    private final CouponReceiveMapper receiveMapper;
    private final CouponService couponService;

    @Override
    protected BaseMapper<CouponReceive> mapper() {
        return receiveMapper;
    }

    @Override
    protected Class<CouponReceive> clazz() {
        return CouponReceive.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"receive_no"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"use_status", "receive_channel", "coupon_id", "member_id"};
    }

    @Override
    protected String orderColumn() {
        return "receive_time";
    }

    /** 会员领券：幂等，重复领取返回已领取记录 */
    @PostMapping("/receive/{couponId}")
    public R<CouponReceive> receive(@PathVariable Long couponId, @RequestParam Long memberId) {
        return R.ok(couponService.receive(couponId, memberId));
    }
}
