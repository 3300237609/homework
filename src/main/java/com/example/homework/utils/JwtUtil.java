package com.example.homework.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    // JWT 密钥（建议配置在 application.yml 中）
    @Value("${jwt.secret}")
    private String secret;
    // 令牌有效期（毫秒，示例：2小时）
    @Value("${jwt.expire}")
    private long expire;

    // 生成 JWT 令牌
    public String generateToken(String userId, Long roleId) {
        Date now = new Date();
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        Date expireTime = new Date(now.getTime() + expire);

        // 载荷（可存储用户信息、权限等）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roleId", roleId);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expireTime)
                .signWith(key)
                .compact();
    }

    // 解析 JWT 令牌
    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 验证令牌是否有效
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        }
    }
}