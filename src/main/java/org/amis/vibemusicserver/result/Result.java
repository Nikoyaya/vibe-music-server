package org.amis.vibemusicserver.result;

/**
 * @author : KwokChichung
 * @description :
 * @createDate : 2026/1/3 16:28
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.amis.vibemusicserver.enumeration.ResultCodeEnum;

/**
 * @param <T>
 * @author : KwokChichung
 * @description : 后端统一返回结果
 * @createDate : 2026/1/3 16:28
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {

    private Integer code;   // 业务状态码
    private String message; // 提示信息
    private T data;         // 响应数据


    // ==================== 成功相关方法 ====================

    // 快速返回操作成功响应结果(默认成功状态码和消息)
    public static Result success() {
        return new Result(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(), null);
    }

    // 快速返回操作成功响应结果(带响应数据)
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(), data);
    }

    // 快速返回操作成功响应结果(带自定义提示信息)
    public static Result success(String message) {
        return new Result(ResultCodeEnum.SUCCESS.getCode(), message, null);
    }

    // 快速返回操作成功响应结果(带响应数据和自定义提示信息)
    public static <E> Result<E> success(String message, E data) {
        return new Result<>(ResultCodeEnum.SUCCESS.getCode(), message, data);
    }

    // ==================== 失败相关方法 ====================

    // 快速返回操作失败响应结果(默认错误状态码和消息)
    public static Result error() {
        return new Result(ResultCodeEnum.OPERATION_FAILED.getCode(), ResultCodeEnum.OPERATION_FAILED.getMessage(), null);
    }

    // 快速返回操作失败响应结果(带自定义提示信息)
    public static Result error(String message) {
        return new Result(ResultCodeEnum.OPERATION_FAILED.getCode(), message, null);
    }

    // 根据ResultCodeEnum返回错误结果
    public static Result error(ResultCodeEnum resultCode) {
        return new Result(resultCode.getCode(), resultCode.getMessage(), null);
    }

    // 根据ResultCodeEnum返回错误结果(带响应数据)
    public static <T> Result<T> error(ResultCodeEnum resultCode, T data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    // ==================== 通用结果方法 ====================

    // 根据ResultCodeEnum返回结果
    public static <T> Result<T> result(ResultCodeEnum resultCode, T data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    // 根据ResultCodeEnum返回结果(自定义消息)
    public static <T> Result<T> result(ResultCodeEnum resultCode, String message, T data) {
        return new Result<>(resultCode.getCode(), message, data);
    }
}

