package com.univ.momcare.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析。secret 与过期时间取自 application.yml 的 app.jwt.*
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expire-hours:12}")
    private int expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(Long userId, String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    /** 解析失败返回 null，由过滤器决定是否放行 */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
