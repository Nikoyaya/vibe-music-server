package org.amis.vibemusicserver.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;

/**
 * @author : KwokChichung
 * @description : JWT工具类
 * @createDate : 2026/1/4 4:13
 */
@Slf4j
public class JwtUtil {

    //密钥
    private static final String SECRET_KEY = "secret";
    // 设置 JWT 的过期时间 6 小时
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 6;

    /**
     * 生成 JWT token
     *
     * @param claims 自定义的业务数据
     * @return JWT token
     */
    public static String generateToken(Map<String, Object> claims) {
        // 使用JWT库创建一个新的JWT构建器
        return JWT.create()
                // 添加自定义的业务数据到JWT的声明中
                .withClaim("claims", claims)
                // 设置JWT的过期时间，当前时间加上预设的过期时长
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                // 使用HMAC256算法和预设的密钥对JWT进行签名
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }


    /**
     * 解析 JWT token
     *
     * @param token JWT token
     * @return 自定义的业务数据，解析失败时抛出异常
     * @throws JWTVerificationException JWT验证失败时抛出
     */
    public static Map<String, Object> parseToken(String token) {
        // 检查token是否为null或空字符串
        if (token == null || token.trim().isEmpty()) {
            // 构造错误信息，记录错误日志，并抛出JWT验证异常
            String tokenError = String.format("Token cannot be null or empty, received: %s", token);
            log.error(tokenError);
            throw new JWTVerificationException(tokenError);
        }

        try {
            // 使用HMAC256算法和SECRET_KEY构建JWT验证器
            // 验证token并获取其中的"claims"字段，将其转换为Map返回
            return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                    .build()
                    .verify(token)
                    .getClaim("claims")
                    .asMap();
        } catch (JWTVerificationException e) {
            // 捕获JWT验证异常，记录错误日志，并重新抛出异常
            log.error("JWT verification failed for token: {}", token, e);
            throw e;
        }
    }

}