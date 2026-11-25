package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单（表 `sys_menu`）
 * 字段由 sql/init.sql 的 DDL 生成，改动请同步两处。
 */
@Data
@TableName("sys_menu")
public class SysMenu implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单ID，0为根 */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 权限标识 */
    private String perms;

    /** M目录 C菜单 F按钮 */
    private String type;

    /** 图标标识 */
    private String icon;

    /** 排序 */
    private Integer sortNo;

    /** 1显示 0隐藏 */
    private Integer visible;

    /** 正常/停用 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 0正常 1删除 */
    @TableLogic
    private Integer deleted;

    private static final long serialVersionUID = 1L;
}