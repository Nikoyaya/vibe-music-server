package org.amis.vibemusicserver.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author : KwokChichung
 * @description : Token传输对象(包含双Token及过期时间)
 * @createDate : 2026/1/9 17:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO implements Serializable {
    /**
     * 访问令牌(6小时过期)
     */
    private String accessToken;

    /**
     * 刷新令牌(15天过期)
     */
    private String refreshToken;

    /**
     * access_token过期时间
     */
    private LocalDateTime accessTokenExpireTime;

    /**
     * refresh_token过期时间
     */
    private LocalDateTime refreshTokenExpireTime;

    public TokenDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpireTime = LocalDateTime.now().plusHours(6);
        this.refreshTokenExpireTime = LocalDateTime.now().plusDays(15);
    }
}

