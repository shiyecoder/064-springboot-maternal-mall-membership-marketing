package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * baby_profile（表 `baby_profile`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("baby_profile")
public class BabyProfile implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则 BB+会员ID后4位+2位序号，全局唯一 */
    private String babyNo;

    /** 关联 member.id，一个会员最多5个宝宝 */
    private Long memberId;

    /** 1至15字 */
    private String babyName;

    /** 男/女/未知 */
    private String gender;

    /** 不得晚于当前日期，修改需二次确认 */
    private LocalDate birthday;

    /** 0至72，每晚按出生日期重算，不允许手工录入 */
    private Integer monthAge;

    /** 30.0至130.0 */
    private BigDecimal heightCm;

    /** 1.0至40.0，纸尿裤选码参考 */
    private BigDecimal weightKg;

    /** 如牛奶蛋白过敏，最多100字 */
    private String allergyDesc;

    /** 1段/2段/3段/4段/未使用 */
    private String milkStage;

    /** S/M/L/XL/XXL/未使用 */
    private String diaperSize;

    /** 1有效 0停用，停用后不参与适龄推荐 */
    private Integer status;

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
