package org.amis.vibemusicserver.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.model.dto.UserRegisterDTO;
import org.amis.vibemusicserver.model.dto.VerificationCodeDTO;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/4 18:23
 */

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 发送验证码
     *
     * @param email 邮箱
     * @return 结果
     */
    @GetMapping("/sendVerificationCode")
    public Result sendVerificationCode(@RequestParam @Email String email) {
        return userService.sendVerificationCode(email);
    }

    /**
     * 验证邮箱验证码（仅用作开发调试）
     * @param verificationCodeDTO 验证码信息
     *                            email: 邮箱
     *                            code: 验证码
     *                            注意：此接口仅用于开发调试，生产环境应通过注册账号方法验证验证码。
     * @return 验证结果
     */
    @PostMapping("/verifyVerificationCode")
    public Result verifyVerificationCode(@RequestBody VerificationCodeDTO verificationCodeDTO) {
        // 验证验证码是否正确
        boolean isCodeValid = userService.verifyVerificationCode(verificationCodeDTO.getEmail(), verificationCodeDTO.getVerificationCode());
        if (!isCodeValid) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }
        return Result.success(MessageConstant.VERIFICATION_CODE + MessageConstant.SUCCESS);
    }

    /**
     * 注册
     *
     * @param userRegisterDTO 用户注册信息
     * @param bindingResult   绑定结果
     * @return 结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody @Valid UserRegisterDTO userRegisterDTO, BindingResult bindingResult) {
        return null;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login() {
        return null;
    }

    /**
     * 用户登出
     */
    @RequestMapping("/logout")
    public Result logout() {
        return null;
    }
}

