package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.SysDictData;
import com.univ.momcare.entity.SysDictType;
import com.univ.momcare.mapper.SysDictDataMapper;
import com.univ.momcare.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典：页面下拉统一从字典接口取值，禁止前后端各自硬编码枚举
 */
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final SysDictTypeMapper typeMapper;
    private final SysDictDataMapper dataMapper;

    @GetMapping("/types")
    public R<List<SysDictType>> types() {
        return R.ok(typeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getStatus, "启用").orderByAsc(SysDictType::getId)));
    }

    @GetMapping("/{dictType}")
    public R<List<SysDictData>> items(@PathVariable String dictType) {
        return R.ok(dataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, "启用")
                .orderByAsc(SysDictData::getSortNo)));
    }

    /** 一次性返回全部字典，减少首屏请求数 */
    @GetMapping("/all")
    public R<java.util.Map<String, List<SysDictData>>> all() {
        List<SysDictData> all = dataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getStatus, "启用").orderByAsc(SysDictData::getSortNo));
        java.util.Map<String, List<SysDictData>> m = new java.util.LinkedHashMap<>();
        for (SysDictData d : all) {
            m.computeIfAbsent(d.getDictType(), k -> new java.util.ArrayList<>()).add(d);
        }
        return R.ok(m);
    }
}
