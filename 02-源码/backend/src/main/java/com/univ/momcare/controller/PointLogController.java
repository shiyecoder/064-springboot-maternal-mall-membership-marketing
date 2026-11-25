package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.entity.PointLog;
import com.univ.momcare.mapper.PointLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 积分流水：只读列表 */
@RestController
@RequestMapping("/api/point-log")
@RequiredArgsConstructor
public class PointLogController extends BaseCrudController<PointLog> {

    private final PointLogMapper pointLogMapper;

    @Override
    protected BaseMapper<PointLog> mapper() {
        return pointLogMapper;
    }

    @Override
    protected Class<PointLog> clazz() {
        return PointLog.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"point_no", "biz_no", "remark"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"change_type", "member_id"};
    }
}
