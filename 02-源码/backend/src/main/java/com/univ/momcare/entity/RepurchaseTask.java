package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * repurchase_task（表 `repurchase_task`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("repurchase_task")
public class RepurchaseTask implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则 FG+yyMMdd+4位序号，全局唯一 */
    private String remindNo;

    /** 关联 member.id，同一商品7天内只一条 */
    private Long memberId;

    /** 关联 baby_goods.id，须为奶粉或纸尿裤类 */
    private Long goodsId;

    /** 取该商品最近一次已完成订单 */
    private LocalDate lastBuyDate;

    /** 按规格与日均用量估算，不得早于上次购买日期 */
    private LocalDate predictEndDate;

    /** 站内消息/短信模拟/导购电话 */
    private String remindChannel;

    /** 预计用完日期前3天，不得早于当前日期 */
    private LocalDateTime planTime;

    /** 待发送/已发送/已跳过/发送失败 */
    private String sendStatus;

    /** 已复购/咨询未购/无响应/退订 */
    private String responseResult;

    /** 关联 sys_user.id，导购跟进时填写 */
    private Long ownerId;

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

    /** 关联名称，列表展示用，不落库 */
    @TableField(exist = false)
    private String ownerName;


    private static final long serialVersionUID = 1L;
}
