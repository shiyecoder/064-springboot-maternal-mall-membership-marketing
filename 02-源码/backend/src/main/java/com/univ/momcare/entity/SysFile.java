package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件（表 `sys_file`）
 * 字段由 sql/init.sql 的 DDL 生成，改动请同步两处。
 */
@Data
@TableName("sys_file")
public class SysFile implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 相对路径 /uploads/... */
    private String filePath;

    /** 字节数 */
    private Long fileSize;

    /** 扩展名 */
    private String fileType;

    /** 业务类型 */
    private String bizType;

    /** 业务主键 */
    private Long bizId;

    /** 上传人 */
    private Long uploaderId;

    /** 创建时间 */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}