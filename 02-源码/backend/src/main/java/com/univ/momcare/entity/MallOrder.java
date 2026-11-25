package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * mall_order（表 `mall_order`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("mall_order")
public class MallOrder implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则 MO+yyMMdd+6位序号，全局唯一 */
    private String orderNo;

    /** 关联 member.id，须为正常状态 */
    private Long memberId;

    /** 1至20，后端校验 */
    private Integer itemCount;

    /** 会员价或售价乘件数合计 */
    private BigDecimal goodsAmount;

    /** 关联 coupon_receive.id，一单限用一张 */
    private Long couponReceiveId;

    /** 不超过商品金额，券门槛需满足 */
    private BigDecimal discountAmount;

    /** 满199免运费，否则8.00 */
    private BigDecimal freight;

    /** 商品金额加运费减优惠，不得为负 */
    private BigDecimal payAmount;

    /** 实付金额取整，退款完成后按比例扣回 */
    private Integer gainPoint;

    /** 微信/支付宝/储值卡，仅登记 */
    private String payType;

    /** 待支付/待发货/已发货/已完成/退货中/已退款/已取消 */
    private String status;

    /** 收货人（下单时快照） */
    private String receiverName;

    /** 收货手机号 */
    private String receiverMobile;

    /** 收货地址，发货后不可修改 */
    private String receiverAddress;

    /** 退款/取消原因，必填不少于5字 */
    private String refundReason;

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

    /** 订单明细，详情接口返回，不落库 */
    @TableField(exist = false)
    private java.util.List<MallOrderItem> items;


    private static final long serialVersionUID = 1L;
}
