package org.amis.vibemusicserver.enumeration;

import lombok.Getter;

/**
 * 用户角色枚举类
 * 定义系统中可用的用户角色类型
 *
 * @author KwokChichung
 */
@Getter
public enum RoleEnum {
    /**
     * 管理员角色
     * 拥有系统的最高权限，可以执行所有操作
     */
    ADMIN("ROLE_ADMIN"),

    /**
     * 普通用户角色
     * 拥有基本的系统操作权限
     */
    USER("ROLE_USER");

    private final String role;

    /**
     * 枚举构造函数
     * @param role 角色标识字符串
     */
    RoleEnum(String role) {
        this.role = role;
    }
}