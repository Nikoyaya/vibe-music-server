package org.amis.vibemusicserver.controller;

import org.amis.vibemusicserver.model.dto.TokenDTO;
import org.amis.vibemusicserver.model.dto.TokenRefreshDTO;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.TokenService;
import org.amis.vibemusicserver.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TokenService tokenService;

    /**
     * 刷新AccessToken
     * @param refreshDTO 包含refreshToken的DTO
     * @return 新的TokenDTO(包含新accessToken和原refreshToken)
     * @throws RuntimeException 当refreshToken无效时抛出
     */
    @PostMapping("/refresh")
    public Result<TokenDTO> refreshToken(@RequestBody TokenRefreshDTO refreshDTO) {
        return tokenService.generateRefreshToken(refreshDTO);
    }
}

