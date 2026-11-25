package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色（表 `sys_role`）
 * 字段由 sql/init.sql 的 DDL 生成，改动请同步两处。
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 数据范围 1本人 2本部门 3全部 */
    private Integer dataScope;

    /** 排序 */
    private Integer sortNo;

    /** 正常/停用 */
    private String status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 0正常 1删除 */
    @TableLogic
    private Integer deleted;

    private static final long serialVersionUID = 1L;
}