package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.entity.SysFile;
import com.univ.momcare.mapper.SysFileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件元数据：标准 CRUD（分页 / 详情 / 新增 / 修改 / 删除）
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController extends BaseCrudController<SysFile> {

    private final SysFileMapper sysFileMapper;

    @Override
    protected BaseMapper<SysFile> mapper() {
        return sysFileMapper;
    }

    @Override
    protected Class<SysFile> clazz() {
        return SysFile.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"file_name"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"biz_type"};
    }

    @Override
    protected String orderColumn() {
        return "create_time";
    }
}
