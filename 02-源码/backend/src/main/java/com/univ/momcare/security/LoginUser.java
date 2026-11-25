package com.univ.momcare.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 登录上下文：用户基础信息 + 角色编码 + 数据范围 */
@Data
@AllArgsConstructor
public class LoginUser implements Serializable {
    private Long userId;
    private String username;
    private String realName;
    private Long deptId;
    private List<String> roles;
    /** 1本人 2本部门 3全部 */
    private Integer dataScope;
}
