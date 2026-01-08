package org.amis.vibemusicserver.model.dto;

import lombok.Data;
import org.amis.vibemusicserver.enumeration.UserStatusEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/8 20:58
 */
@Data
public class UserSearchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页显示条数
     */
    private Integer pageSize;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户状态：0-启用，1-禁用
     */
    private UserStatusEnum userStatus;
}

