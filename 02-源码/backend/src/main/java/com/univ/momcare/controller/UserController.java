package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.entity.SysUser;
import com.univ.momcare.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理：标准 CRUD（分页 / 详情 / 新增 / 修改 / 删除）
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController extends BaseCrudController<SysUser> {

    private final SysUserMapper sysUserMapper;

    @Override
    protected BaseMapper<SysUser> mapper() {
        return sysUserMapper;
    }

    @Override
    protected Class<SysUser> clazz() {
        return SysUser.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"username", "real_name", "user_no", "phone"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"status", "dept_id"};
    }

    @Override
    protected String orderColumn() {
        return "create_time";
    }
}
