package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.GoodsReview;
import com.univ.momcare.mapper.GoodsReviewMapper;
import com.univ.momcare.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/** 商品评价审核：只审核合规性，差评不得删除，仅隐藏违规内容 */
@RestController
@RequestMapping("/api/goods-review")
@RequiredArgsConstructor
public class ReviewController extends BaseCrudController<GoodsReview> {

    private final GoodsReviewMapper reviewMapper;

    @Override
    protected BaseMapper<GoodsReview> mapper() {
        return reviewMapper;
    }

    @Override
    protected Class<GoodsReview> clazz() {
        return GoodsReview.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"content"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"status", "goods_id", "member_id", "score"};
    }

    /** 审核：通过或驳回，必须给出处理意见 */
    @PostMapping("/{id}/audit")
    public R<GoodsReview> audit(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (!"已通过".equals(status) && !"已驳回".equals(status)) {
            throw new BizException("审核结果只能是已通过或已驳回");
        }
        GoodsReview r = reviewMapper.selectById(id);
        if (r == null) {
            throw new BizException("评价不存在");
        }
        r.setStatus(status);
        r.setReply(body.get("reply"));
        r.setAuditBy(CurrentUser.id());
        r.setAuditTime(LocalDateTime.now());
        reviewMapper.updateById(r);
        return R.ok(r);
    }
}
