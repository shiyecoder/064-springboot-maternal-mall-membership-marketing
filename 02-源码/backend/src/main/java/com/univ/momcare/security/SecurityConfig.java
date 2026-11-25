package com.univ.momcare.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 无状态 JWT 鉴权。放行登录、上传静态资源与健康检查，其余需携带 token。
 * 写接口的角色校验在各 Controller 内按数据范围复核，前端隐藏按钮不等于授权。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .cors(c -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/login", "/api/auth/captcha", "/api/public/**").permitAll()
                .requestMatchers("/uploads/**", "/error", "/favicon.ico").permitAll()
                .requestMatchers("/v3/api-docs/**", "/doc.html", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, resp, ex) -> {
                    resp.setStatus(HttpStatus.UNAUTHORIZED.value());
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"code\":4010,\"message\":\"登录失效，请重新登录\",\"data\":null}");
                })
                .accessDeniedHandler((req, resp, ex) -> {
                    resp.setStatus(HttpStatus.FORBIDDEN.value());
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"code\":4030,\"message\":\"无接口权限\",\"data\":null}");
                }))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
