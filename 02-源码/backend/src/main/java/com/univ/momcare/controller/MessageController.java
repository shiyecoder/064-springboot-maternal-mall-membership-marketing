package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.entity.SysMessage;
import com.univ.momcare.mapper.SysMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内消息：标准 CRUD（分页 / 详情 / 新增 / 修改 / 删除）
 */
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController extends BaseCrudController<SysMessage> {

    private final SysMessageMapper sysMessageMapper;

    @Override
    protected BaseMapper<SysMessage> mapper() {
        return sysMessageMapper;
    }

    @Override
    protected Class<SysMessage> clazz() {
        return SysMessage.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"title"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"read_flag", "msg_level", "biz_type"};
    }

    @Override
    protected String orderColumn() {
        return "create_time";
    }
}
