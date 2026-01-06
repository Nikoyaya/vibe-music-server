package org.amis.vibemusicserver.enumeration;

/**
 * 业务结果状态码枚举
 * 定义系统所有返回结果的状态码和消息
 */
public enum ResultCodeEnum {

    /**
     * 成功状态码
     */
    SUCCESS(200, "操作成功"),

    /**
     * 客户端错误状态码 (4xx)
     */
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    /**
     * 服务器错误状态码 (5xx)
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    /**
     * 业务错误状态码 (自定义业务异常)
     */
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_LOGIN_FAILED(1003, "用户名或密码错误"),
    USER_ACCOUNT_LOCKED(1004, "用户账户被锁定"),
    USER_NOT_LOGIN(1005, "用户未登录"),
    TOKEN_EXPIRED(1006, "令牌已过期"),
    TOKEN_INVALID(1007, "令牌无效"),
    PERMISSION_DENIED(1008, "权限不足"),
    NOT_LOGIN(1009, "缺少令牌"),
    SESSION_EXPIRED(1010, "会话已过期"),
    NO_PERMISSION(1011, "无权限访问"),

    /**
     * 数据相关错误
     */
    DATA_NOT_FOUND(2001, "数据不存在"),
    DATA_ALREADY_EXISTS(2002, "数据已存在"),
    DATA_VALIDATION_FAILED(2003, "数据验证失败"),

    /**
     * 文件操作相关错误
     */
    FILE_UPLOAD_FAILED(3001, "文件上传失败"),
    FILE_NOT_FOUND(3002, "文件不存在"),
    FILE_SIZE_EXCEEDED(3003, "文件大小超出限制"),
    FILE_TYPE_NOT_SUPPORTED(3004, "文件类型不支持"),

    /**
     * 第三方服务错误
     */
    THIRD_PARTY_SERVICE_ERROR(4001, "第三方服务错误"),
    NETWORK_ERROR(4002, "网络连接错误"),

    /**
     * 通用业务错误
     */
    BUSINESS_ERROR(5001, "业务逻辑错误"),
    OPERATION_FAILED(5002, "操作失败"),
    SYSTEM_BUSY(5003, "系统繁忙，请稍后重试");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态消息
     */
    private final String message;

    /**
     * 构造方法
     * @param code 状态码
     * @param message 状态消息
     */
    ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取状态码
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取状态消息
     * @return 状态消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 根据状态码获取对应的枚举实例
     * @param code 状态码
     * @return 对应的ResultCode枚举实例
     */
    public static ResultCodeEnum getByCode(int code) {
        for (ResultCodeEnum resultCodeEnum : ResultCodeEnum.values()) {
            if (resultCodeEnum.getCode() == code) {
                return resultCodeEnum;
            }
        }
        return null;
    }

    /**
     * 判断状态码是否表示成功（2xx状态码）
     * @param code 状态码
     * @return 是否成功
     */
    public static boolean isSuccess(int code) {
        return code >= 200 && code < 300;
    }
}
