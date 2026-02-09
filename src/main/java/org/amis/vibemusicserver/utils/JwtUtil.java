package org.amis.vibemusicserver.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.constant.JwtClaimsConstant;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
/**
 * @author : KwokChichung
 * @description : JWT工具类
 * @createDate : 2026/1/4 4:13
 */
@Slf4j
public class JwtUtil {
    private static final String SECRET_KEY = "secret";
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 60 * 6; // 6小时
    private static final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 15; // 15天

    /**
     * 生成access_token
     */
    public static String generateAccessToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("type", "access")
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    /**
     * 生成refresh_token
     */
    public static String generateRefreshToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("type", "refresh")
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    /**
     * 解析JWT token
     */
    public static Map<String, Object> parseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            String error = "Token cannot be null or empty";
            log.error(error);
            throw new JWTVerificationException(error);
        }

        try {
            return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token)
                    .getClaim("claims")
                    .asMap();
        } catch (JWTVerificationException e) {
            log.error("JWT verification failed for token: {}", token, e);
            throw e;
        }
    }

    /**
     * 验证是否为refresh_token
     */
    public static boolean isRefreshToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token);
            return "refresh".equals(jwt.getClaim("type").asString());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中获取用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Map<String, Object> claims = parseToken(token);
        return (Long) claims.get(JwtClaimsConstant.USER_ID);
    }

    /**
     * 获取token的过期时间
     */
    public static LocalDateTime getExpirationFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token);
            return LocalDateTime.ofInstant(
                    jwt.getExpiresAt().toInstant(),
                    ZoneId.systemDefault()
            );
        } catch (Exception e) {
            log.error("获取token过期时间失败: {}", token, e);
            throw new RuntimeException("无法解析token过期时间");
        }
    }
}