package org.amis.vibemusicserver.model.vo;

import lombok.Data;
import org.amis.vibemusicserver.enumeration.UserStatusEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;

/**
 * @author : KwokChichung
 * @description : 用户管理视图对象
 * @createDate : 2026/1/8 20:52
 */
@Data
public class UserManagementVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String introduction;

    /**
     * 用户注册时间
     */
    private LocalTime createTime;

    /**
     * 用户更新时间
     */
    private LocalTime updateTime;

    /**
     * 用户状态：0-启用，1-禁用
     */
    private UserStatusEnum userStatus;
}

