package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.entity.SysRole;
import com.univ.momcare.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色管理：标准 CRUD（分页 / 详情 / 新增 / 修改 / 删除）
 */
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController extends BaseCrudController<SysRole> {

    private final SysRoleMapper sysRoleMapper;

    @Override
    protected BaseMapper<SysRole> mapper() {
        return sysRoleMapper;
    }

    @Override
    protected Class<SysRole> clazz() {
        return SysRole.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"role_name", "role_code"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"status"};
    }

    @Override
    protected String orderColumn() {
        return "sort_no";
    }
}
