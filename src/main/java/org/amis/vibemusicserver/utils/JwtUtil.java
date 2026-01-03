package org.amis.vibemusicserver.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.util.Date;
import java.util.Map;

/**
 * @author : KwokChichung
 * @description : JWT工具类
 * @createDate : 2026/1/4 4:13
 */
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
        //JWT生成逻辑
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
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
        if (token == null || token.trim().isEmpty()) {
            throw new JWTVerificationException("Token cannot be null or empty");
        }

        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

}