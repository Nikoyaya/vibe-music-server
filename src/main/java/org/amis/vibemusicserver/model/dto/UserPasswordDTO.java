package org.amis.vibemusicserver.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.amis.vibemusicserver.constant.MessageConstant;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : KwokChichung
 * @description : 用户密码DTO
 * @createDate : 2026/1/6 20:41
 */
@Data
public class UserPasswordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 旧密码（RSA加密后的字符串）
     */
    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    private String oldPassword;

    /**
     * 新密码（RSA加密后的字符串）
     */
    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    private String newPassword;

    /**
     * 确认密码（RSA加密后的字符串）
     */
    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    private String repeatPassword;

}

