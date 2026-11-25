package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志（表 `sys_oper_log`）
 * 字段由 sql/init.sql 的 DDL 生成，改动请同步两处。
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模块 */
    private String module;

    /** 操作类型 */
    private String operType;

    /** 业务单号 */
    private String bizNo;

    /** 操作内容 */
    private String content;

    /** 操作人 */
    private Long operId;

    /** 操作人姓名 */
    private String operName;

    /** 操作时间 */
    private LocalDateTime operTime;

    /** IP */
    private String ip;

    /** 成功/失败/拒绝 */

    private static final long serialVersionUID = 1L;
}