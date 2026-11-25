package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * mall_order_item（表 `mall_order_item`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("mall_order_item")
public class MallOrderItem implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 mall_order.id */
    private Long orderId;

    /** 关联 baby_goods.id */
    private Long goodsId;

    /** 下单时商品名称快照 */
    private String goodsName;

    /** 下单时规格快照 */
    private String specDesc;

    /** 成交单价（会员价优先） */
    private BigDecimal price;

    /** 数量1至20 */
    private Integer qty;

    /** 单价乘数量 */
    private BigDecimal amount;

    /** 关联 sys_user.id */
    private Long createBy;

    /** 记录创建时间 */
    private LocalDateTime createTime;

    /** 关联 sys_user.id */
    private Long updateBy;

    /** 更新时自动刷新 */
    private LocalDateTime updateTime;

    /** 0正常 1删除 */
    @TableLogic
    private Integer deleted;

    /** 乐观锁 */
    private Integer version;


    private static final long serialVersionUID = 1L;
}
