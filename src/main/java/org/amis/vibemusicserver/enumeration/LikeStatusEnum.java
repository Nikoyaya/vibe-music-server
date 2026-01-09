package org.amis.vibemusicserver.enumeration;

import lombok.Getter;

/**
 * @author : KwokChichung
 * @description : 喜欢状态枚举
 */

@Getter
public enum LikeStatusEnum {

    DEFAULT(0, "默认"),
    LIKE(1, "喜欢");

    private final Integer code;
    private final String likeStatus;

    LikeStatusEnum(Integer code, String likeStatus) {
        this.code = code;
        this.likeStatus = likeStatus;
    }
}
