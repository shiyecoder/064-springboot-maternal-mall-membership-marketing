package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * point_log（表 `point_log`）
 * 字段与 sql/init.sql 的 DDL 一致，改动请同步两处。
 */
@Data
@TableName("point_log")
public class PointLog implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则 JF+yyMMdd+6位序号，全局唯一 */
    private String pointNo;

    /** 关联 member.id */
    private Long memberId;

    /** 下单获得/评价获得/签到/兑换扣减/退款扣回/人工调整 */
    private String changeType;

    /** 正数增加、负数扣减，不得为0 */
    private Integer changePoint;

    /** 0至999999 */
    private Integer beforePoint;

    /** 等于变动前积分加变动值，不得为负 */
    private Integer afterPoint;

    /** 关联订单号或兑换单号 */
    private String bizNo;

    /** 人工调整时必填，不少于5字 */
    private String remark;

    /** 关联 sys_user.id，系统自动时为空 */
    private Long operatorId;

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
    private String operatorName;


    private static final long serialVersionUID = 1L;
}
