package org.amis.vibemusicserver.enumeration;

import lombok.Getter;

/**
 * 用户状态枚举类
 * <p>
 * 定义系统中用户的状态类型
 * <p/>
 *
 * @author KwokChichung
 */
@Getter
public enum UserStatusEnum {
    /**
     * 用户启用状态
     */
    Enable(0, "启用"),
    /**
     * 用户未禁用状态
     */
    DISABLE(1, "禁用");

    private final Integer code;
    private final String userStatus;

    UserStatusEnum(Integer code, String userStatus) {
        this.code = code;
        this.userStatus = userStatus;
    }
}
