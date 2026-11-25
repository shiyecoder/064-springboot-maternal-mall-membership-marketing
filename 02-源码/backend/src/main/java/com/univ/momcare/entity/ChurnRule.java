package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * churn_rule（表 `churn_rule`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("churn_rule")
public class ChurnRule implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则名称 */
    private String ruleName;

    /** 版本号 v1/v2/v3，全局唯一 */
    private String ruleVersion;

    /** 最近购买间隔权重，合计须为100 */
    private Integer weightGap;

    /** 近90天消费频次权重 */
    private Integer weightFreq;

    /** 客单价变化率权重 */
    private Integer weightAmount;

    /** 优惠券使用率权重 */
    private Integer weightCoupon;

    /** 宝宝月龄阶段跨度权重 */
    private Integer weightAge;

    /** 高风险阈值，风险分大于等于此值判高风险 */
    private Integer highThreshold;

    /** 中风险阈值 */
    private Integer midThreshold;

    /** 草稿/生效/历史 */
    private String status;

    /** 生效时间，历史版本保留不覆盖 */
    private LocalDateTime effectTime;

    /** 变更说明，不少于5字 */
    private String remark;

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
