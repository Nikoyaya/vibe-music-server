package org.amis.vibemusicserver.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.amis.vibemusicserver.annotation.RequestDebounce;
import org.amis.vibemusicserver.constant.MessageConstant;
import org.amis.vibemusicserver.model.dto.*;
import org.amis.vibemusicserver.model.vo.UserVO;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IUserService;
import org.amis.vibemusicserver.service.MinioService;
import org.amis.vibemusicserver.utils.BindingResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MinioService minioService;

    /**
     * 发送验证码
     *
     * @param email 邮箱
     * @return 结果
     */
    @RequestDebounce(
        key = "sendVerificationCode",
        expire = 60,  // 60秒内防止重复发送
        message = "验证码发送过于频繁，请1分钟后再试"
    )
    @GetMapping("/sendVerificationCode")
    public Result sendVerificationCode(@RequestParam @Email String email) {
        return userService.sendVerificationCode(email);
    }

    /**
     * 验证邮箱验证码（仅用作开发调试）
     *
     * @param verificationCodeDTO 验证码信息
     *                            email: 邮箱
     *                            code: 验证码
     *                            注意：此接口仅用于开发调试，生产环境应通过注册账号方法验证验证码。
     * @return 验证结果
     */
    @PostMapping("/verifyVerificationCode")
    public Result verifyVerificationCode(@RequestBody @Valid VerificationCodeDTO verificationCodeDTO) {
        // 验证验证码是否正确
        boolean isCodeValid = userService.verifyVerificationCode(verificationCodeDTO.getEmail(), verificationCodeDTO.getVerificationCode());
        if (!isCodeValid) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }
        // 删除Redis中的验证码
        stringRedisTemplate.delete("verificationCode:" + verificationCodeDTO.getEmail());
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
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (bindingResult.hasErrors()) {
            return Result.error(errorMessage);
        }

        Boolean isCodeValid = userService.verifyVerificationCode(userRegisterDTO.getEmail(), userRegisterDTO.getVerificationCode());
        if (!isCodeValid) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }
        return Result.success(userService.register(userRegisterDTO));
    }

    /**
     * 登录
     *
     * @param userLoginDTO  用户登录信息
     * @param bindingResult 绑定结果
     * @return 结果
     */
    @PostMapping("/login")
    public Result login(@RequestBody @Valid UserLoginDTO userLoginDTO, BindingResult bindingResult) {
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (bindingResult.hasErrors()) {
            return Result.error(errorMessage);
        }
        return userService.login(userLoginDTO);
    }

    /**
     * 用户登出
     *
     * @param token 认证token
     * @return 结果
     */
    @RequestMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        return userService.logout(token);
    }

    /**
     * 更新用户密码
     *
     * @param userPasswordDTO 用户密码信息
     * @param token           认证token
     * @return 结果
     */
    @PatchMapping("/updateUserPassword")
    public Result updateUserPassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO,
                                     @RequestHeader("Authorization") String token, BindingResult bindingResult) {
        // 处理参数校验结果
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (bindingResult.hasErrors()) {
            return Result.error(errorMessage);
        }
        // 调用服务层更新密码
        return userService.updateUserPassword(userPasswordDTO, token);
    }

    /**
     * 重置用户密码
     *
     * @param userResetPasswordDTO 用户密码信息
     * @return 结果
     */
    @PatchMapping("/resetUserPassword")
    public Result resetUserPassword(@RequestBody @Valid UserResetPasswordDTO userResetPasswordDTO, BindingResult bindingResult) {
        // 校验DTO参数，如果校验失败则返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        // 验证邮箱验证码是否有效
        Boolean isCodeValid = userService.verifyVerificationCode(userResetPasswordDTO.getEmail(), userResetPasswordDTO.getVerificationCode());

        // 如果验证码无效，返回错误信息
        if (!isCodeValid) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }

        // 调用服务层重置用户密码
        return userService.resetUserPassword(userResetPasswordDTO);
    }

    /**
     * 获取用户信息
     *
     * @return 结果
     */
    @GetMapping("/getUserInfo")
    public Result<UserVO> getUserInfo() {
        return userService.userInfo();
    }

    /**
     * 更新用户信息
     *
     * @param userDTO       用户信息
     * @param bindingResult 绑定结果
     * @return 结果
     */
    @PutMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {
        // 校验请求数据，如果校验失败则返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (bindingResult.hasErrors()) {
            return Result.error(errorMessage);
        }
        // 调用服务层更新用户信息
        return userService.updateUserInfo(userDTO);
    }

    /**
     * 更新用户头像
     *
     * @param avatar 用户头像文件
     * @return 结果
     */
    @PatchMapping("/updateUserAvatar")
    public Result updateUserAvatar(@RequestParam("avatar") MultipartFile avatar) {
        String userAvatar = minioService.uploadFile(avatar, "userAvatar");
        return userService.updateUserAvatar(userAvatar);
    }

    /**
     * 注销账户
     *
     * @return 结果
     */
    @DeleteMapping("/deleteAccount")
    public Result deleteAccount() {
        return userService.deleteAccount();
    }
}

