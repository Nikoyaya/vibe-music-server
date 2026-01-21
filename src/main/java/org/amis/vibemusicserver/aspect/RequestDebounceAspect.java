package org.amis.vibemusicserver.aspect;

import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.annotation.RequestDebounce;
import org.amis.vibemusicserver.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.amis.vibemusicserver.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

/**
 * @author : KwokChichung
 * @description : 请求防抖切面
 * @createDate : 2026/1/21
 */
@Slf4j
@Aspect
@Component
public class RequestDebounceAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${request.debounce.enabled:true}")
    private boolean debounceEnabled;

    @Value("${request.debounce.default-expire:60}")
    private int defaultExpireSeconds;

    private static final String DEBOUNCE_PREFIX = "debounce:";

    @Around("@annotation(debounceAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, RequestDebounce debounceAnnotation) throws Throwable {
        // 如果防抖功能未启用，直接执行业务方法
        if (!debounceEnabled) {
            log.debug("防抖功能已禁用，直接执行业务方法");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        // 生成防抖key
        String debounceKey = generateDebounceKey(debounceAnnotation, joinPoint);

        // 使用注解的expire值，如果未设置则使用配置的默认值
        int expireSeconds = debounceAnnotation.expire() > 0 ?
                debounceAnnotation.expire() : defaultExpireSeconds;

        // 检查是否重复请求
        if (isDuplicateRequest(debounceKey, expireSeconds)) {
            log.warn("请求防抖拦截: key={}, method={}, expire={}s", debounceKey, methodName, expireSeconds);
            return Result.error(debounceAnnotation.message());
        }

        try {
            // 执行业务方法
            return joinPoint.proceed();
        } finally {
            // 可以在这里清理防抖key，或者依靠Redis自动过期
        }
    }

    /**
     * 生成防抖key
     */
    private String generateDebounceKey(RequestDebounce annotation, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String keyPrefix = annotation.key().isEmpty() ? methodName : annotation.key();

        // 获取请求参数信息
        String paramsHash = generateParamsHash(joinPoint.getArgs());

        // 获取用户信息（如果有）
        String userInfo = getUserInfo();

        // 构建完整的防抖key
        return DEBOUNCE_PREFIX + keyPrefix + ":" + userInfo + ":" + paramsHash;
    }

    /**
     * 生成请求参数哈希
     */
    private String generateParamsHash(Object[] args) {
        if (args == null || args.length == 0) {
            return "no_params";
        }

        try {
            // 简单哈希，避免敏感信息泄漏
            String paramsStr = Arrays.toString(args);
            return String.valueOf(paramsStr.hashCode());
        } catch (Exception e) {
            return "params_error";
        }
    }

    /**
     * 获取用户标识信息
     */
    private String getUserInfo() {
        try {
            // 从ThreadLocal中获取用户信息
            Map<String, Object> claims = ThreadLocalUtil.get();
            if (claims != null && claims.containsKey("userId")) {
                Object userId = claims.get("userId");
                return "user_" + (userId != null ? userId.toString() : "null");
            }

            // 尝试从请求头中解析JWT token
            // 这里需要根据实际项目调整，可能需要注入HttpServletRequest
            return "user_unknown";
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
            return "user_error";
        }
    }

    /**
     * 检查是否为重复请求
     */
    private boolean isDuplicateRequest(String key, int expireSeconds) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(
                key,
                "1",
                Duration.ofSeconds(expireSeconds)
        );
        return !Boolean.TRUE.equals(result);
    }
}