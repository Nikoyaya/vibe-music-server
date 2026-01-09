package org.amis.vibemusicserver.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author : KwokChichung
 * @description : Token刷新DTO
 * @createDate : 2026/1/9 17:57
 */
@Data
public class TokenRefreshDTO {
    /**
     * 刷新令牌(必须)
     */
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}

