package com.univ.momcare.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.entity.SysMenu;
import com.univ.momcare.entity.SysRole;
import com.univ.momcare.entity.SysUser;
import com.univ.momcare.entity.SysUserRole;
import com.univ.momcare.mapper.SysMenuMapper;
import com.univ.momcare.mapper.SysRoleMapper;
import com.univ.momcare.mapper.SysUserMapper;
import com.univ.momcare.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录用户装载：角色、数据范围、菜单。
 * 菜单按「角色 → sys_role_menu → sys_menu（type=M/C 且 visible=1）」动态获取，
 * 系统管理员的「菜单管理」项对非超管隐藏（需求文档硬性要求 12）。
 */
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;

    public LoginUser loadLoginUser(Long userId) {
        SysUser u = userMapper.selectById(userId);
        if (u == null || !"正常".equals(u.getStatus())) {
            return null;
        }
        List<SysRole> roles = rolesOf(userId);
        List<String> codes = new ArrayList<>();
        int scope = 1;
        for (SysRole r : roles) {
            codes.add(r.getRoleCode());
            scope = Math.max(scope, r.getDataScope() == null ? 1 : r.getDataScope());
        }
        return new LoginUser(u.getId(), u.getUsername(), u.getRealName(), u.getDeptId(), codes, scope);
    }

    public List<SysRole> rolesOf(Long userId) {
        List<SysUserRole> urs = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (urs.isEmpty()) {
            return List.of();
        }
        List<Long> ids = urs.stream().map(SysUserRole::getRoleId).toList();
        return roleMapper.selectBatchIds(ids);
    }

    /** 当前用户的菜单树（只返回目录与菜单，按钮单独返回权限标识） */
    public List<SysMenu> menusOf(Long userId, boolean superAdmin) {
        List<SysRole> roles = rolesOf(userId);
        if (roles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        List<Long> menuIds = new ArrayList<>();
        for (Long rid : roleIds) {
            menuMapper.selectMenuIdsByRole(rid).forEach(menuIds::add);
        }
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<SysMenu> all = menuMapper.selectBatchIds(menuIds.stream().distinct().toList());
        List<SysMenu> out = new ArrayList<>();
        for (SysMenu m : all) {
            if (!"M".equals(m.getType()) && !"C".equals(m.getType())) continue;
            if (m.getVisible() != null && m.getVisible() == 0) continue;
            // 非超管隐藏「菜单管理」，避免越权修改权限树
            if (!superAdmin && "menu:view".equals(m.getPerms())) continue;
            out.add(m);
        }
        out.sort((a, b) -> {
            int c = Long.compare(a.getParentId() == null ? 0 : a.getParentId(),
                                 b.getParentId() == null ? 0 : b.getParentId());
            return c != 0 ? c : Integer.compare(a.getSortNo() == null ? 0 : a.getSortNo(),
                                                b.getSortNo() == null ? 0 : b.getSortNo());
        });
        return out;
    }

    public List<String> permsOf(Long userId) {
        List<SysRole> roles = rolesOf(userId);
        List<String> out = new ArrayList<>();
        for (SysRole r : roles) {
            if ("ADMIN".equals(r.getRoleCode())) {
                out.add("*:*:*");
            }
            for (Long mid : menuMapper.selectMenuIdsByRole(r.getId())) {
                SysMenu m = menuMapper.selectById(mid);
                if (m != null && m.getPerms() != null && !m.getPerms().isBlank()) {
                    out.add(m.getPerms());
                }
            }
        }
        return out.stream().distinct().toList();
    }
}
