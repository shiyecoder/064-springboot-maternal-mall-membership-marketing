package com.univ.momcare.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 解析 Authorization: Bearer &lt;token&gt;，把 LoginUser 放进 SecurityContext。
 * token 缺失或无效时不做拦截，交给 SecurityConfig 的鉴权规则处理（返回 401）。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserAuthService userAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            Claims c = jwtUtil.parse(auth.substring(7));
            if (c != null) {
                Long uid = Long.valueOf(c.getSubject());
                LoginUser lu = userAuthService.loadLoginUser(uid);
                if (lu != null) {
                    List<SimpleGrantedAuthority> auths = lu.getRoles() == null ? List.of()
                            : lu.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                              .collect(Collectors.toList());
                    UsernamePasswordAuthenticationToken t =
                            new UsernamePasswordAuthenticationToken(lu, null, auths);
                    SecurityContextHolder.getContext().setAuthentication(t);
                }
            }
        }
        chain.doFilter(req, resp);
    }
}
