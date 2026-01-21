package org.amis.vibemusicserver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author : KwokChichung
 * @description : 请求防抖注解
 * @createDate : 2026/1/21
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestDebounce {

    /**
     * 防抖key前缀（默认使用方法名）
     */
    String key() default "";

    /**
     * 防抖过期时间（秒）
     */
    int expire() default 60;

    /**
     * 错误消息
     */
    String message() default "请求过于频繁，请稍后再试";
}