package org.amis.vibemusicserver.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.amis.vibemusicserver.constant.MessageConstant;

/**
 * @author : KwokChichung
 * @description :验证码验证DTO
 * <p>
 * (用于开发调试邮箱验证)
 * <p/>
 */
@Data
public class VerificationCodeDTO {
    /**
     * 用户邮箱
     */
    @NotBlank(message = MessageConstant.EMAIL + MessageConstant.NOT_NULL)
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    private String email;

    /**
     * 验证码
     * 验证码格式：6位字符（大小写字母、数字）
     */
    @NotBlank(message = MessageConstant.VERIFICATION_CODE + MessageConstant.NOT_NULL)
    @Pattern(regexp = "^[0-9a-zA-Z]{6}$", message = MessageConstant.VERIFICATION_CODE + MessageConstant.FORMAT_ERROR)
    private String verificationCode;
}