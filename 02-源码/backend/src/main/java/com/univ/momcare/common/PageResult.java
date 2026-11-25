package com.univ.momcare.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.util.List;

/**
 * 分页响应：records/total/page/size/pages，页码从 1 开始
 */
@Data
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long page;
    private long size;
    private long pages;

    public static <T> PageResult<T> of(IPage<T> p) {
        PageResult<T> r = new PageResult<>();
        r.records = p.getRecords();
        r.total = p.getTotal();
        r.page = p.getCurrent();
        r.size = p.getSize();
        r.pages = p.getPages();
        return r;
    }

    public static <T> PageResult<T> of(List<T> list) {
        PageResult<T> r = new PageResult<>();
        r.records = list;
        r.total = list.size();
        r.page = 1;
        r.size = list.size();
        r.pages = 1;
        return r;
    }
}
