package com.example.demo_ai.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 提供者（基于 jjwt 0.12.6）
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret:demo-ai-secret-key-for-jwt-token-generation}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")  // 默认 24 小时
    private long jwtExpirationInMs;

    /**
     * 生成 JWT Token
     */
    public String generateToken(String username, String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("获取 Token 中的用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public String getUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("userId", String.class);
        } catch (Exception e) {
            logger.error("获取 Token 中的用户 ID 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            logger.error("Token 验证失败: {}", e.getMessage());
            return false;
        }
    }
}

