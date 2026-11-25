package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

/**
 * 角色菜单（表 `sys_role_menu`）
 * 字段由 sql/init.sql 的 DDL 生成，改动请同步两处。
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 菜单ID */
    private Long menuId;

    private static final long serialVersionUID = 1L;
}