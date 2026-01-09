package org.amis.vibemusicserver.controller;

import org.amis.vibemusicserver.model.dto.TokenDTO;
import org.amis.vibemusicserver.model.dto.TokenRefreshDTO;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.utils.JwtUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author : KwokChichung
 * @description : Token管理控制器
 * @createDate : 2026/1/9 17:56
 */
@RestController
@RequestMapping("/token")
public class TokenController {

    /**
     * 刷新AccessToken
     * @param refreshDTO 包含refreshToken的DTO
     * @return 新的TokenDTO(包含新accessToken和原refreshToken)
     * @throws RuntimeException 当refreshToken无效时抛出
     */
    @PostMapping("/refresh")
    public Result<TokenDTO> refreshToken(@RequestBody TokenRefreshDTO refreshDTO) {
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

