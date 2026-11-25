package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告（表 `sys_notice`）
 * 字段由 sql/init.sql 的 DDL 生成，改动请同步两处。
 */
@Data
@TableName("sys_notice")
public class SysNotice implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 通知/预警/公告 */
    private String noticeType;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 发布人 */
    private Long publisherId;

    /** 草稿/已发布/已下线 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 0正常 1删除 */
    @TableLogic
    private Integer deleted;

    /** 关联名称（非表字段，列表接口回填） */
    @TableField(exist = false)
    private String publisherName;

    private static final long serialVersionUID = 1L;
}