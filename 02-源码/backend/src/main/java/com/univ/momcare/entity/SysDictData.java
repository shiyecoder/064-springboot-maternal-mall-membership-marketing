package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

/**
 * 字典数据（表 `sys_dict_data`）
 * 字段由 sql/init.sql 的 DDL 生成，改动请同步两处。
 */
@Data
@TableName("sys_dict_data")
public class SysDictData implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 字典类型编码 */
    private String dictType;

    /** 字典标签 */
    private String dictLabel;

    /** 字典值 */
    private String dictValue;

    /** 标签样式 ok/warn/danger/info/mute */
    private String tagType;

    /** 排序 */
    private Integer sortNo;

    /** 启用/停用 */
    private String status;

    private static final long serialVersionUID = 1L;
}