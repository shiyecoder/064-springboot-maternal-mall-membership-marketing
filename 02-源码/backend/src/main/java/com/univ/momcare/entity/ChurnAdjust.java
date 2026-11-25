package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * churn_adjust（表 `churn_adjust`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("churn_adjust")
public class ChurnAdjust implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 churn_score.id，原始快照不被覆盖 */
    private Long scoreId;

    /** 关联 member.id */
    private Long memberId;

    /** 修正前等级 */
    private String originLevel;

    /** 修正前分数 */
    private BigDecimal originScore;

    /** 修正后等级 */
    private String newLevel;

    /** 修正原因，不少于5字 */
    private String reason;

    /** 修正人 sys_user.id */
    private Long adjustBy;

    /** 修正时间 */
    private LocalDateTime adjustTime;

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


    private static final long serialVersionUID = 1L;
}
