package org.amis.vibemusicserver.service.impl;

import org.amis.vibemusicserver.model.dto.TokenDTO;
import org.amis.vibemusicserver.model.dto.TokenRefreshDTO;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.TokenService;
import org.amis.vibemusicserver.utils.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/9 23:21
 */
@Service
public class TokenServiceImpl implements TokenService {
    @Override
    public Result<TokenDTO> generateRefreshToken(TokenRefreshDTO refreshDTO) {
        String refreshToken = refreshDTO.getRefreshToken();

        // 验证refresh_token有效性
        if (!JwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的refresh token");
        }

        // 解析用户信息
        Map<String, Object> claims = JwtUtil.parseToken(refreshToken);

        // 生成新的access_token
        String newAccessToken = JwtUtil.generateAccessToken(claims);

        // 返回新access_token和原refresh_token
        return Result.success(new TokenDTO(newAccessToken, refreshToken));
    }
}

