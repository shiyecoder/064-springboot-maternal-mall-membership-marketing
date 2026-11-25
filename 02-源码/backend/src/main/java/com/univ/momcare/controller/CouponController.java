package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.Coupon;
import com.univ.momcare.mapper.CouponMapper;
import com.univ.momcare.service.CodeGenService;
import com.univ.momcare.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 优惠券配置 + 定向发放 */
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController extends BaseCrudController<Coupon> {

    private final CouponMapper couponMapper;
    private final CodeGenService codeGen;
    private final CouponService couponService;

    @Override
    protected BaseMapper<Coupon> mapper() {
        return couponMapper;
    }

    @Override
    protected Class<Coupon> clazz() {
        return Coupon.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"coupon_no", "coupon_name"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"coupon_type", "status", "grant_type", "limit_type"};
    }

    @Override
    @PostMapping
    public R<Long> add(@RequestBody Coupon body) {
        if (body.getValidStart() == null || body.getValidEnd() == null) {
            throw new BizException("请填写有效期起止");
        }
        if (!body.getValidEnd().isAfter(body.getValidStart())) {
            throw new BizException("有效期结束必须晚于开始时间");
        }
        if (body.getValidStart().plusDays(180).isBefore(body.getValidEnd())) {
            throw new BizException("券有效期最长 180 天");
        }
        if ("满减".equals(body.getCouponType())
                && body.getMinAmount().compareTo(body.getDiscountAmount()) <= 0) {
            throw new BizException("满减券门槛必须大于优惠金额");
        }
        if (body.getTotalQty() == null || body.getTotalQty() < 1 || body.getTotalQty() > 100000) {
            throw new BizException("发放总量需在 1 到 100000 之间");
        }
        body.setCouponNo(codeGen.couponNo());
        body.setReceivedQty(0);
        if (body.getStatus() == null) {
            body.setStatus("待发布");
        }
        if (body.getDiscountAmount() == null) {
            body.setDiscountAmount(BigDecimal.ZERO);
        }
        return super.add(body);
    }

    /** 定向发券：按等级 + 风险 + 购买间隔筛选，返回实发/跳过/成本 */
    @PostMapping("/{id}/grant")
    public R<Map<String, Object>> grant(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> levels = (List<String>) body.get("levels");
        @SuppressWarnings("unchecked")
        List<String> risks = (List<String>) body.get("risks");
        Integer minGap = body.get("minGapDays") == null ? null : Integer.valueOf(String.valueOf(body.get("minGapDays")));
        Integer limit = body.get("limit") == null ? null : Integer.valueOf(String.valueOf(body.get("limit")));
        return R.ok(couponService.grant(id, levels, risks, minGap, limit));
    }

    /** 到期券处理 */
    @PostMapping("/expire")
    public R<Integer> expire() {
        return R.ok(couponService.expireAll());
    }
}
