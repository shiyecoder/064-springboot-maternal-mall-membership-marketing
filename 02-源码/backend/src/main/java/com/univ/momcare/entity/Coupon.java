package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * coupon（表 `coupon`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("coupon")
public class Coupon implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则 QY+yyMMdd+4位序号，全局唯一 */
    private String couponNo;

    /** 最多30字，如金卡专享满300减50 */
    private String couponName;

    /** 满减/折扣/立减/免邮 */
    private String couponType;

    /** 0.00至9999.00，满减券必须大于优惠金额 */
    private BigDecimal minAmount;

    /** 满减与立减填金额，折扣券填折扣值 */
    private BigDecimal discountAmount;

    /** 全场通用/指定品类/指定商品 */
    private String limitType;

    /** 指定品类的品类名或指定商品ID，全场通用为空 */
    private String limitValue;

    /** 主动领取/定向发放/新人礼包/积分兑换 */
    private String grantType;

    /** 与有效期结束成对填写 */
    private LocalDateTime validStart;

    /** 必须晚于开始时间，最长180天 */
    private LocalDateTime validEnd;

    /** 1至100000，后端校验 */
    private Integer totalQty;

    /** 不得超过发放总量，并发领取需加锁 */
    private Integer receivedQty;

    /** 待发布/发放中/已停发/已过期 */
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
