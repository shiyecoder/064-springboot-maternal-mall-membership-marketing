package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.RepurchaseTask;
import com.univ.momcare.mapper.RepurchaseTaskMapper;
import com.univ.momcare.service.RepurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 复购提醒台：列表 + 批量生成 + 登记跟进 */
@RestController
@RequestMapping("/api/repurchase-task")
@RequiredArgsConstructor
public class RepurchaseController extends BaseCrudController<RepurchaseTask> {

    private final RepurchaseTaskMapper repurchaseMapper;
    private final RepurchaseService repurchaseService;

    @Override
    protected BaseMapper<RepurchaseTask> mapper() {
        return repurchaseMapper;
    }

    @Override
    protected Class<RepurchaseTask> clazz() {
        return RepurchaseTask.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"remind_no"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"send_status", "response_result", "remind_channel", "member_id", "goods_id", "owner_id"};
    }

    @Override
    protected String orderColumn() {
        return "plan_time";
    }

    @PostMapping("/generate")
    public R<Map<String, Object>> generate() {
        return R.ok(repurchaseService.generate());
    }

    @PostMapping("/{id}/follow")
    public R<RepurchaseTask> follow(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(repurchaseService.follow(id, body.get("responseResult"), body.get("remark")));
    }
}
