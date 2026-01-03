package org.amis.vibemusicserver.controller;

import jakarta.validation.Valid;
import org.amis.vibemusicserver.model.dto.AdminDTO;
import org.amis.vibemusicserver.result.Result;
import org.amis.vibemusicserver.service.IAdminService;
import org.amis.vibemusicserver.utils.BindingResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/3 16:09
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IAdminService adminService;


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
            return Result.error(errorMessage);
        }
        return adminService.register(adminDTO);
    }
}

