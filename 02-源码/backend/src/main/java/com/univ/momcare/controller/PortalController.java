package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.CouponReceive;
import com.univ.momcare.entity.GoodsReview;
import com.univ.momcare.mapper.CouponReceiveMapper;
import com.univ.momcare.mapper.GoodsReviewMapper;
import com.univ.momcare.security.CurrentUser;
import com.univ.momcare.service.CouponService;
import com.univ.momcare.service.PortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 会员门户：只返回当前登录会员本人的数据 */
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;
    private final CouponService couponService;
    private final CouponReceiveMapper receiveMapper;
    private final GoodsReviewMapper reviewMapper;

    @GetMapping("/home")
    public R<Map<String, Object>> home() {
        return R.ok(portalService.home(CurrentUser.id()));
    }

    @GetMapping("/my")
    public R<Map<String, Object>> my() {
        return R.ok(portalService.myCenter(CurrentUser.id()));
    }

    @GetMapping("/my-coupons")
    public R<List<CouponReceive>> myCoupons() {
        return R.ok(receiveMapper.selectList(new LambdaQueryWrapper<CouponReceive>()
                .eq(CouponReceive::getMemberId, CurrentUser.id())
                .orderByDesc(CouponReceive::getReceiveTime)));
    }

    /** 领券：同一活动限 1 张 */
    @PostMapping("/receive/{couponId}")
    public R<CouponReceive> receive(@PathVariable Long couponId) {
        return R.ok(couponService.receive(couponId, CurrentUser.id()));
    }

    /** 提交评价（已完成的订单） */
    @PostMapping("/review")
    public R<Long> review(@RequestBody GoodsReview body) {
        body.setMemberId(CurrentUser.id());
        body.setStatus("待审核");
        reviewMapper.insert(body);
        return R.ok(body.getId());
    }
}
