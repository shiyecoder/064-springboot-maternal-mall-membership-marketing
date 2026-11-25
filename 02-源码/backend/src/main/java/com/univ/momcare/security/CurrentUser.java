package com.univ.momcare.security;

import com.univ.momcare.common.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** 当前登录人工具类。未登录时抛 4010。 */
public final class CurrentUser {
    private CurrentUser() {}

    public static LoginUser get() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof LoginUser lu)) {
            throw new BizException(4010, "登录失效，请重新登录");
        }
        return lu;
    }

    public static Long id() {
        return get().getUserId();
    }

    public static boolean hasRole(String role) {
        LoginUser u = get();
        return u.getRoles() != null && u.getRoles().contains(role);
    }
}
