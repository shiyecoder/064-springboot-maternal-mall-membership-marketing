package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.SysRoleMenu;
import com.univ.momcare.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 为角色分配菜单：读取当前授权 + 覆盖式保存。
 * 前端隐藏按钮不等于授权，写接口在此处整体替换，避免残留越权授权。
 */
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleMenuController {

    private final SysRoleMenuMapper roleMenuMapper;

    @GetMapping("/{roleId}/menus")
    public R<List<Long>> menus(@PathVariable Long roleId) {
        return R.ok(roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList());
    }

    @PutMapping("/{roleId}/menus")
    @Transactional
    public R<Boolean> save(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null) {
            for (Long mid : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(mid);
                roleMenuMapper.insert(rm);
            }
        }
        return R.ok(true);
    }
}
