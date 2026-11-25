package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.entity.SysOperLog;
import com.univ.momcare.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志：标准 CRUD（分页 / 详情 / 新增 / 修改 / 删除）
 */
@RestController
@RequestMapping("/api/oper-log")
@RequiredArgsConstructor
public class OperLogController extends BaseCrudController<SysOperLog> {

    private final SysOperLogMapper sysOperLogMapper;

    @Override
    protected BaseMapper<SysOperLog> mapper() {
        return sysOperLogMapper;
    }

    @Override
    protected Class<SysOperLog> clazz() {
        return SysOperLog.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"biz_no", "content", "oper_name"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"module", "oper_type", "result"};
    }

    @Override
    protected String orderColumn() {
        return "oper_time";
    }
}
