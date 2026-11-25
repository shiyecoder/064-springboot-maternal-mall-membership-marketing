package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.SysMenu;
import com.univ.momcare.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理：树形列表 + 增删改。
 * 「菜单管理」本身及其分配入口对非超管隐藏（AuthService 已过滤）。
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final SysMenuMapper menuMapper;

    @GetMapping("/tree")
    public R<List<SysMenu>> tree() {
        return R.ok(menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getParentId).orderByAsc(SysMenu::getSortNo)));
    }

    @PostMapping
    public R<Long> add(@RequestBody SysMenu body) {
        if (body.getParentId() == null) body.setParentId(0L);
        menuMapper.insert(body);
        return R.ok(body.getId());
    }

    @PutMapping("/{id}")
    public R<Boolean> edit(@PathVariable Long id, @RequestBody SysMenu body) {
        body.setId(id);
        return R.ok(menuMapper.updateById(body) > 0);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        Long children = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        if (children != null && children > 0) {
            throw new com.univ.momcare.common.BizException("请先删除子菜单");
        }
        return R.ok(menuMapper.deleteById(id) > 0);
    }
}
