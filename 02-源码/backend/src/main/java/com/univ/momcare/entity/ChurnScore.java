package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * churn_score（表 `churn_score`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("churn_score")
public class ChurnScore implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 member.id */
    private Long memberId;

    /** 关联 churn_rule.id */
    private Long ruleId;

    /** 计算时使用的规则版本 */
    private String ruleVersion;

    /** 输入快照 JSON，保证同输入同结果 */
    private String inputJson;

    /** 最近购买间隔得分 */
    private Integer scoreGap;

    /** 消费频次得分 */
    private Integer scoreFreq;

    /** 客单价变化得分 */
    private Integer scoreAmount;

    /** 券使用率得分 */
    private Integer scoreCoupon;

    /** 月龄跨度得分 */
    private Integer scoreAge;

    /** 加权总分，越高流失风险越大 */
    private BigDecimal totalScore;

    /** 低风险/中风险/高风险/已流失 */
    private String riskLevel;

    /** 推荐结论，展示主要影响因素 */
    private String conclusion;

    /** 未满足条件/数据缺口说明 */
    private String unmet;

    /** 可改进项与召回建议 */
    private String suggest;

    /** 分配的导购 sys_user.id */
    private Long ownerId;

    /** 待召回/跟进中/已召回/无需召回 */
    private String recallStatus;

    /** 计算时间 */
    private LocalDateTime calcTime;

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

    /** 关联名称，列表展示用，不落库 */
    @TableField(exist = false)
    private String memberName;

    /** 关联名称，列表展示用，不落库 */
    @TableField(exist = false)
    private String ownerName;


    private static final long serialVersionUID = 1L;
}
