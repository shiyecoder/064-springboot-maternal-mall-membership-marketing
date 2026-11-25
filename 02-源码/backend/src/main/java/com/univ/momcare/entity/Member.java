package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * member（表 `member`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("member")
public class Member implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则 HY+yyMMdd+4位序号，全局唯一 */
    private String memberNo;

    /** 11位，全局唯一，同时作为登录账号 */
    private String mobile;

    /** 2至20字，敏感词过滤 */
    private String nickName;

    /** 普通会员/银卡/金卡/黑卡 */
    private String levelCode;

    /** 门店扫码/小程序/导购推荐 */
    private String registerChannel;

    /** 已完成订单实付累计，退款完成后回减 */
    private BigDecimal totalAmount;

    /** 已完成订单数，退款完成后回减 */
    private Integer orderCount;

    /** 0至999999，兑换与退款扣回，不得为负 */
    private Integer pointBalance;

    /** 用于流失预警计算，从未购买时为空 */
    private LocalDateTime lastBuyTime;

    /** 0至5，由宝宝档案有效条数维护 */
    private Integer babyCount;

    /** 低风险/中风险/高风险/已流失/未评估 */
    private String riskLevel;

    /** 正常/冻结/注销 */
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

    /** 关联名称，列表展示用，不落库 */
    @TableField(exist = false)
    private String memberName;


    private static final long serialVersionUID = 1L;
}
