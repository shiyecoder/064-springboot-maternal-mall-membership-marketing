package com.univ.momcare.controller;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.common.R;
import com.univ.momcare.entity.BabyGoods;
import com.univ.momcare.mapper.BabyGoodsMapper;
import com.univ.momcare.service.CodeGenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/** 母婴商品：适龄标签、会员价校验、库存与上架状态联动 */
@RestController
@RequestMapping("/api/baby-goods")
@RequiredArgsConstructor
public class BabyGoodsController extends BaseCrudController<BabyGoods> {

    private final BabyGoodsMapper goodsMapper;
    private final CodeGenService codeGen;

    @Override
    protected BaseMapper<BabyGoods> mapper() {
        return goodsMapper;
    }

    @Override
    protected Class<BabyGoods> clazz() {
        return BabyGoods.class;
    }

    @Override
    protected String[] keywordColumns() {
        return new String[]{"goods_no", "goods_name", "brand"};
    }

    @Override
    protected String[] filterColumns() {
        return new String[]{"goods_type", "status", "brand"};
    }

    @Override
    @PostMapping
    public R<Long> add(@RequestBody BabyGoods body) {
        validate(body);
        body.setGoodsNo(codeGen.goodsNo());
        if (body.getStatus() == null) {
            body.setStatus(body.getStockQty() != null && body.getStockQty() > 0 ? "在售" : "待上架");
        }
        return super.add(body);
    }

    @Override
    @PutMapping("/{id}")
    public R<Boolean> edit(@PathVariable Long id, @RequestBody BabyGoods body) {
        validate(body);
        if (body.getStockQty() != null && body.getStockQty() == 0 && "在售".equals(body.getStatus())) {
            body.setStatus("已售罄");
        }
        return super.edit(id, body);
    }

    /** 补货并恢复在售（库存为 0 时自动售罄，补货后需运营手动上架） */
    @PostMapping("/{id}/restock")
    public R<BabyGoods> restock(@PathVariable Long id, @RequestParam Integer qty) {
        if (qty == null || qty <= 0) {
            throw new BizException("补货数量必须大于 0");
        }
        BabyGoods g = goodsMapper.selectById(id);
        if (g == null) {
            throw new BizException("商品不存在");
        }
        g.setStockQty((g.getStockQty() == null ? 0 : g.getStockQty()) + qty);
        g.setStatus("在售");
        goodsMapper.updateById(g);
        return R.ok(g);
    }

    private void validate(BabyGoods g) {
        if (g.getAgeMonthStart() != null && g.getAgeMonthEnd() != null
                && g.getAgeMonthStart() > g.getAgeMonthEnd()) {
            throw new BizException("适用月龄起不得大于止");
        }
        if (g.getSalePrice() == null || g.getSalePrice().compareTo(new BigDecimal("0.01")) < 0) {
            throw new BizException("售价必须大于 0");
        }
        if (g.getMemberPrice() != null && g.getMemberPrice().compareTo(g.getSalePrice()) > 0) {
            throw new BizException("会员价不得高于售价");
        }
    }
}
