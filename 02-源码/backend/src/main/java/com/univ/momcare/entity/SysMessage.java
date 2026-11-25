package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息（表 `sys_message`）
 * 字段由 sql/init.sql 的 DDL 生成，改动请同步两处。
 */
@Data
@TableName("sys_message")
public class SysMessage implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收人 */
    private Long receiverId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 业务类型 */
    private String bizType;

    /** 业务主键 */
    private Long bizId;

    /** 普通/重要/紧急 */
    private String msgLevel;

    /** 0未读 1已读 */
    private Integer readFlag;

    /** 创建时间 */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}