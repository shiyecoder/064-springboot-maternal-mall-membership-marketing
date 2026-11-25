package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * goods_review（表 `goods_review`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("goods_review")
public class GoodsReview implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 mall_order.id，一单一评 */
    private Long orderId;

    /** 关联 baby_goods.id */
    private Long goodsId;

    /** 关联 member.id */
    private Long memberId;

    /** 1至5星 */
    private Integer score;

    /** 评价内容，5至500字 */
    private String content;

    /** 晒图相对路径，逗号分隔，最多3张 */
    private String images;

    /** 门店回复 */
    private String reply;

    /** 待审核/已通过/已驳回 */
    private String status;

    /** 审核人 sys_user.id */
    private Long auditBy;

    /** 审核时间 */
    private LocalDateTime auditTime;

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

    /** 关联名称，列表展示用，不落库 */
    @TableField(exist = false)
    private String memberName;

    /** 关联名称，列表展示用，不落库 */
    @TableField(exist = false)
    private String goodsName;


    private static final long serialVersionUID = 1L;
}
