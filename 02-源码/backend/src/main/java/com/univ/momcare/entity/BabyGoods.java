package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * baby_goods（表 `baby_goods`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("baby_goods")
public class BabyGoods implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则 MY+yyMMdd+4位序号，全局唯一 */
    private String goodsNo;

    /** 2至60字，含品牌与规格 */
    private String goodsName;

    /** 须在品牌字典中存在 */
    private String brand;

    /** 奶粉/纸尿裤/辅食/洗护/玩具/孕妇用品 */
    private String goodsType;

    /** 0至72，不得大于适用月龄止 */
    private Integer ageMonthStart;

    /** 0至72，与起始月龄共同决定适龄标签 */
    private Integer ageMonthEnd;

    /** 如900g每罐、L码42片 */
    private String specDesc;

    /** 0.01至9999.99，后端校验 */
    private BigDecimal salePrice;

    /** 不得高于售价，为空时按售价结算 */
    private BigDecimal memberPrice;

    /** 0至9999，下单扣减、取消回补 */
    private Integer stockQty;

    /** 商品主图，本地相对路径 /uploads/seed/goods/NN.jpg */
    private String coverImage;

    /** 待上架/在售/已售罄/已下架 */
    private String status;

    /** 关联 sys_user.id */
    private Long createBy;

    /** 记录创建时间 */
    private LocalDateTime createTime;

    /** 关联 sys_user.id */
    private Long updateBy;

    /** 更新时自动刷新 */
    private LocalDateTime updateTime;

    /** 0正常 1删除，查询默认过滤 */
    @TableLogic
    private Integer deleted;

    /** 乐观锁，更新时校验并加1 */
    private Integer version;


    private static final long serialVersionUID = 1L;
}
