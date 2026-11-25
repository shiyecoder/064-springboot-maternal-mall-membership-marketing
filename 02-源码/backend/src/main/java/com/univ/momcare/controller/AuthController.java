package com.univ.momcare.controller;

import com.univ.momcare.common.R;
import com.univ.momcare.dto.LoginDTO;
import com.univ.momcare.dto.PasswordDTO;
import com.univ.momcare.entity.SysMenu;
import com.univ.momcare.entity.SysUser;
import com.univ.momcare.security.CurrentUser;
import com.univ.momcare.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 认证与个人信息：登录、当前用户、菜单、改密、改资料
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        return R.ok(authService.me(CurrentUser.id()));
    }

    @GetMapping("/menus")
    public R<List<SysMenu>> menus() {
        return R.ok(authService.menus(CurrentUser.id()));
    }

    @PostMapping("/password")
    public R<Void> changePassword(@Valid @RequestBody PasswordDTO dto) {
        authService.changePassword(CurrentUser.id(), dto);
        return R.ok();
    }

    @PutMapping("/profile")
    public R<Map<String, Object>> updateProfile(@RequestBody SysUser body) {
        return R.ok(AuthService.safeUser(authService.updateProfile(CurrentUser.id(), body)));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }
}
