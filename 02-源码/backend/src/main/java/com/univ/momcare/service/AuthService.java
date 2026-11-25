package com.univ.momcare.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.univ.momcare.common.BizException;
import com.univ.momcare.dto.LoginDTO;
import com.univ.momcare.dto.PasswordDTO;
import com.univ.momcare.entity.SysMenu;
import com.univ.momcare.entity.SysUser;
import com.univ.momcare.mapper.SysUserMapper;
import com.univ.momcare.security.JwtUtil;
import com.univ.momcare.security.LoginUser;
import com.univ.momcare.security.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录、当前用户信息、菜单与权限。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final UserAuthService userAuthService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> login(LoginDTO dto) {
        // 账号或手机号均可登录：需求文档 5.2 规定会员手机号即登录账号
        String acc = dto.getUsername() == null ? "" : dto.getUsername().trim();
        SysUser u = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .and(w -> w.eq(SysUser::getUsername, acc).or().eq(SysUser::getPhone, acc))
                .last("limit 1"));
        if (u == null || !passwordEncoder.matches(dto.getPassword(), u.getPassword())) {
            throw new BizException(4000, "账号或密码错误");
        }
        if (!"正常".equals(u.getStatus())) {
            throw new BizException(4030, "账号已停用，请联系管理员");
        }
        boolean superAdmin = userAuthService.rolesOf(u.getId()).stream()
                .anyMatch(r -> "ADMIN".equals(r.getRoleCode()));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("token", jwtUtil.generate(u.getId(), u.getUsername()));
        out.put("user", safeUser(u));
        out.put("roles", userAuthService.rolesOf(u.getId()).stream().map(r -> r.getRoleCode()).toList());
        out.put("menus", userAuthService.menusOf(u.getId(), superAdmin));
        out.put("perms", userAuthService.permsOf(u.getId()));
        return out;
    }

    public Map<String, Object> me(Long userId) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) throw new BizException(4010, "登录失效");
        boolean superAdmin = userAuthService.rolesOf(userId).stream()
                .anyMatch(r -> "ADMIN".equals(r.getRoleCode()));
        LoginUser lu = userAuthService.loadLoginUser(userId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("user", safeUser(u));
        out.put("roles", lu == null ? List.of() : lu.getRoles());
        out.put("dataScope", lu == null ? 1 : lu.getDataScope());
        out.put("menus", userAuthService.menusOf(userId, superAdmin));
        out.put("perms", userAuthService.permsOf(userId));
        return out;
    }

    public List<SysMenu> menus(Long userId) {
        boolean superAdmin = userAuthService.rolesOf(userId).stream()
                .anyMatch(r -> "ADMIN".equals(r.getRoleCode()));
        return userAuthService.menusOf(userId, superAdmin);
    }

    public void changePassword(Long userId, PasswordDTO dto) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) throw new BizException(4010, "登录失效");
        if (!passwordEncoder.matches(dto.getOldPassword(), u.getPassword())) {
            throw new BizException(4000, "当前密码不正确");
        }
        SysUser upd = new SysUser();
        upd.setId(userId);
        upd.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        upd.setVersion(u.getVersion());
        userMapper.updateById(upd);
    }

    public SysUser updateProfile(Long userId, SysUser body) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) throw new BizException(4010, "登录失效");
        SysUser upd = new SysUser();
        upd.setId(userId);
        upd.setRealName(body.getRealName());
        upd.setPhone(body.getPhone());
        upd.setEmail(body.getEmail());
        upd.setAvatar(body.getAvatar());
        upd.setVersion(u.getVersion());
        userMapper.updateById(upd);
        return userMapper.selectById(userId);
    }

    /** 脱敏：不返回密码 */
    public static Map<String, Object> safeUser(SysUser u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("realName", u.getRealName());
        m.put("deptId", u.getDeptId());
        m.put("deptName", u.getDeptName());
        m.put("phone", u.getPhone());
        m.put("email", u.getEmail());
        m.put("avatar", u.getAvatar());
        m.put("creditScore", u.getCreditScore());
        m.put("status", u.getStatus());
        m.put("createTime", u.getCreateTime());
        return m;
    }
}
