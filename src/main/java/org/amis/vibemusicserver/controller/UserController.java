package org.amis.vibemusicserver.controller;

import org.amis.vibemusicserver.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/4 18:23
 */

@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register() {
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

