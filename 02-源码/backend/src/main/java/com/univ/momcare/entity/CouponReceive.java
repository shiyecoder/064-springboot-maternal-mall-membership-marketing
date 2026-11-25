package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * coupon_receive（表 `coupon_receive`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("coupon_receive")
public class CouponReceive implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则 LQ+yyMMdd+6位序号，全局唯一 */
    private String receiveNo;

    /** 关联 coupon.id，同一会员同一券只能一条 */
    private Long couponId;

    /** 关联 member.id */
    private Long memberId;

    /** 商城领取/导购发放/新人礼包/积分兑换 */
    private String receiveChannel;

    /** 领取时写入，不可修改 */
    private LocalDateTime receiveTime;

    /** 未使用/已使用/已过期/已作废 */
    private String useStatus;

    /** 关联 mall_order.id，核销后必填 */
    private Long useOrderId;

    /** 核销时写入，与使用订单同时存在 */
    private LocalDateTime useTime;

    /** 关联 sys_user.id，导购发放时必填 */
    private Long grantBy;

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
    private String couponName;


    private static final long serialVersionUID = 1L;
}
