package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.univ.momcare.common.BizException;
import com.univ.momcare.common.NameFill;
import com.univ.momcare.common.PageResult;
import com.univ.momcare.common.R;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 通用 CRUD 基类：分页、详情、新增、修改、删除。
 * 子类只需给出 mapper / 实体类 / 可模糊查询列 / 可精确筛选列 / 排序列。
 *
 * 约定（需求文档 7.3）：
 *  - 分页默认按时间倒序，size 上限 100
 *  - 新增由后端生成业务编号，忽略前端传入的 id 与创建信息
 *  - 修改校验 version，不一致返回 4090
 *  - 删除为逻辑删除（@TableLogic 自动处理）
 */
public abstract class BaseCrudController<T> {

    /** 关联名称回填（xxxId → xxxName），对子类统一生效 */
    @Autowired
    protected NameFill nameFill;

    protected abstract BaseMapper<T> mapper();

    protected abstract Class<T> clazz();

    /** 参与 keyword 模糊匹配的数据库列名 */
    protected String[] keywordColumns() {
        return new String[0];
    }

    /** 允许作为等值筛选条件的请求参数名（同时是列名） */
    protected String[] filterColumns() {
        return new String[0];
    }

    /** 默认排序列；无 create_time 的表覆盖为 id */
    protected String orderColumn() {
        return "create_time";
    }

    @GetMapping("/page")
    public R<PageResult<T>> page(@RequestParam(defaultValue = "1") long page,
                                 @RequestParam(defaultValue = "10") long size,
                                 @RequestParam(required = false) String keyword,
                                 HttpServletRequest request) {
        QueryWrapper<T> qw = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String[] cols = keywordColumns();
            if (cols.length > 0) {
                qw.and(w -> {
                    for (int i = 0; i < cols.length; i++) {
                        if (i == 0) {
                            w.like(cols[i], keyword);
                        } else {
                            w.or().like(cols[i], keyword);
                        }
                    }
                });
            }
        }
        for (String col : filterColumns()) {
            String v = request.getParameter(col);
            if (StringUtils.hasText(v)) {
                qw.eq(col, v);
            }
        }
        // 日期区间通用支持：startTime / endTime 作用于创建时间
        String st = request.getParameter("startTime");
        String et = request.getParameter("endTime");
        if (StringUtils.hasText(st)) qw.ge(orderColumn(), st);
        if (StringUtils.hasText(et)) qw.le(orderColumn(), et);

        qw.orderByDesc(orderColumn());
        long s = Math.min(Math.max(size, 1), 100);
        IPage<T> res = mapper().selectPage(new Page<>(Math.max(page, 1), s), qw);
        nameFill.fill(res.getRecords());
        return R.ok(PageResult.of(res));
    }

    @GetMapping("/{id}")
    public R<T> detail(@PathVariable Long id) {
        T t = mapper().selectById(id);
        if (t == null) {
            throw new BizException("记录不存在或已删除");
        }
        nameFill.fill(List.of(t));
        return R.ok(t);
    }

    @PostMapping
    public R<Long> add(@RequestBody T body) {
        mapper().insert(body);
        return R.ok(idOf(body));
    }

    @PutMapping("/{id}")
    public R<Boolean> edit(@PathVariable Long id, @RequestBody T body) {
        setId(body, id);
        // 乐观锁：提交的 version 与库中不一致时 updateById 返回 0
        int rows = mapper().updateById(body);
        if (rows == 0) {
            throw BizException.version("数据已被他人修改，请刷新后重试");
        }
        return R.ok(true);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        if (mapper().selectById(id) == null) {
            throw new BizException("记录不存在");
        }
        return R.ok(mapper().deleteById(id) > 0);
    }

    protected static Object getField(Object o, String name) {
        try {
            Field f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(o);
        } catch (Exception e) {
            return null;
        }
    }

    protected static void setId(Object o, Object val) {
        try {
            Field f = o.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(o, val);
        } catch (Exception ignored) {
        }
    }

    protected static Long idOf(Object o) {
        Object v = getField(o, "id");
        return v == null ? null : ((Number) v).longValue();
    }

    /** 供子类做数据范围校验用 */
    protected static boolean isBlank(String s) {
        return !StringUtils.hasText(s);
    }

    protected static Map<String, String> emptyMap() {
        return Map.of();
    }
}
