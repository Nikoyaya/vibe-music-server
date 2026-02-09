package org.amis.vibemusicserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.model.dto.TokenDTO;
import org.amis.vibemusicserver.model.dto.TokenRefreshDTO;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.TokenService;
import org.amis.vibemusicserver.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/9 23:21
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result<TokenDTO> generateRefreshToken(TokenRefreshDTO refreshDTO) {
        String refreshToken = refreshDTO.getRefreshToken();

        // 验证refresh_token有效性
        if (!JwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException(MessageConstant.TOKEN + MessageConstant.INVALID);
        }

        // 解析用户信息
        Map<String, Object> claims = JwtUtil.parseToken(refreshToken);

        // 生成新的access_token
        String newAccessToken = JwtUtil.generateAccessToken(claims);

        // 清理旧的access_token及其映射关系（如果存在）
        String oldAccessToken = stringRedisTemplate.opsForValue().get("access_token:" + refreshToken);
        if (oldAccessToken != null) {
            stringRedisTemplate.delete(oldAccessToken); // 删除旧的access_token本身
            stringRedisTemplate.delete("access_token:" + refreshToken); // 删除旧的映射关系
        }

        // 存储新的access_token到Redis
        stringRedisTemplate.opsForValue().set(newAccessToken, newAccessToken, 6, TimeUnit.HOURS);
        // 建立refresh_token到access_token的映射（保持refresh_token原始有效期）
        stringRedisTemplate.opsForValue().set(
                "access_token:" + refreshToken,
                newAccessToken,
                15, TimeUnit.DAYS
        );

        // 手动创建TokenDTO，设置正确的过期时间
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setAccessToken(newAccessToken);
        tokenDTO.setRefreshToken(refreshToken);
        tokenDTO.setAccessTokenExpireTime(LocalDateTime.now().plusHours(6));

        // 从refreshToken解析实际过期时间
        tokenDTO.setRefreshTokenExpireTime(JwtUtil.getExpirationFromToken(refreshToken));

        return Result.success(tokenDTO);
    }
}