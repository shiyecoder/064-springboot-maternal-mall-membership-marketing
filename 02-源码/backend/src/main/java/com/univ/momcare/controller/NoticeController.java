package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.entity.SysNotice;
import com.univ.momcare.mapper.SysNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告：标准 CRUD（分页 / 详情 / 新增 / 修改 / 删除）
 */
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController extends BaseCrudController<SysNotice> {

    private final SysNoticeMapper sysNoticeMapper;

    @Override
    protected BaseMapper<SysNotice> mapper() {
        return sysNoticeMapper;
    }

    @Override
    protected Class<SysNotice> clazz() {
        return SysNotice.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"title"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"status", "notice_type"};
    }

    @Override
    protected String orderColumn() {
        return "publish_time";
    }
}
