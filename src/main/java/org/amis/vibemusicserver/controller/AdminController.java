package org.amis.vibemusicserver.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.model.dto.AdminDTO;
import org.amis.vibemusicserver.model.dto.UserAddDTO;
import org.amis.vibemusicserver.model.dto.UserDTO;
import org.amis.vibemusicserver.model.dto.UserSearchDTO;
import org.amis.vibemusicserver.model.vo.UserManagementVO;
import org.amis.vibemusicserver.result.PageResult;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IAdminService;
import org.amis.vibemusicserver.service.IUserService;
import org.amis.vibemusicserver.utils.BindingResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : KwokChichung
 * @description : 管理员控制器类，用于处理与管理员相关的请求。
 * @createDate : 2026/1/3 16:09
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @Autowired
    private IUserService userService;


    /**
     * 注册管理员
     *
     * @param adminDTO      管理员信息
     * @param bindingResult 绑定结果
     * @return 结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody @Valid AdminDTO adminDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            log.warn("管理员注册验证失败: {}", errorMessage);
            return Result.error(errorMessage);
        }
        log.info("管理员注册成功: {}", adminDTO.getUsername());
        return adminService.register(adminDTO);
    }

    /**
     * 登录管理员
     *
     * @param adminDTO      管理员信息
     * @param bindingResult 绑定结果
     * @return 结果
     */
    @PostMapping("/login")
    public Result login(@RequestBody @Valid AdminDTO adminDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            log.warn("管理员登录校验失败: {}", errorMessage);
            return Result.error(errorMessage);
        }
        return adminService.login(adminDTO);

    }

    /**
     * 登出
     *
     * @param token 认证token
     * @return 结果
     */
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        return adminService.logout(token);
    }

    //**********************************************************************************************/

    /**
     * 获取所有用户数量
     *
     * @return 用户数量
     */
    @GetMapping("/getAllUsersCount")
    public Result<Long> getAllUsersCount() {
        return userService.getAllUsersCount();
    }

    /**
     * 获取所有用户信息
     *
     * @param userSearchDTO 用户搜索条件
     * @return 结果
     */
    @PostMapping("/getAllUsers")
    public Result<PageResult<UserManagementVO>> getAllUsers(@RequestBody UserSearchDTO userSearchDTO) {
        return userService.getAllUsers(userSearchDTO);
    }

    /**
     * 添加用户
     *
     * @param userAddDTO    用户信息
     * @param bindingResult 绑定结果
     * @return 结果
     */
    @PostMapping("/addUser")
    public Result addUser(@RequestBody @Valid UserAddDTO userAddDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.addUser(userAddDTO);
    }

    /**
     * 更新用户信息
     *
     * @param userDTO       用户信息
     * @param bindingResult 绑定结果
     * @return 结果
     */
    @PutMapping("/updateUser")
    public Result updateUser(@RequestBody @Valid UserDTO userDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.updateUser(userDTO);
    }

    /**
     * 更新用户状态
     *
     * @param userId     用户ID
     * @param userStatus 用户状态
     * @return 结果
     */
    @PatchMapping("/updateUserStatus/{id}/{status}")
    public Result updateUserStatus(@PathVariable("id") Long userId, @PathVariable("status") Integer userStatus) {
        return userService.updateUserStatus(userId, userStatus);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @DeleteMapping("/deleteUser/{id}")
    public Result deleteUser(@PathVariable("id") Long userId) {
        return userService.deleteUser(userId);
    }

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     * @return 结果
     */
    @DeleteMapping("/deleteUsers")
    public Result deleteUsers(@RequestBody List<Long> userIds) {
        return userService.deleteUsers(userIds);
    }

    //**********************************************************************************************/


}

