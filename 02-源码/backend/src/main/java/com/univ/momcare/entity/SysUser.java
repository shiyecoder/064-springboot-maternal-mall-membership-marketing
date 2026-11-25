package com.univ.momcare.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户（表 `sys_user`）
 * 字段由 sql/init.sql 的 DDL 生成，改动请同步两处。
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** BCrypt 加密后的密码；只写不读，接口响应不会返回 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 真实姓名 */
    private String realName;


    /** 所属院系/部门ID */
    private Long deptId;

    /** 所属院系/部门名称 */
    private String deptName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像相对路径 */
    private String avatar;

    /** 信用分，违规扣减 */
    private Integer creditScore;

    /** 正常/停用 */
    private String status;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private Long createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新人 */
    private Long updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 0正常 1删除 */
    @TableLogic
    private Integer deleted;

    /** 乐观锁 */
    private Integer version;

    private static final long serialVersionUID = 1L;
}