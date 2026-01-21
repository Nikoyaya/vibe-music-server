package org.amis.vibemusicserver.aspect;

import lombok.extern.slf4j.Slf4j;
import org.amis.vibemusicserver.annotation.RequestDebounce;
import org.amis.vibemusicserver.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

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
        String keyPrefix = annotation.key();
        if (keyPrefix.isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            keyPrefix = signature.getMethod().getName();
        }

        // 可以在这里添加更多维度，如用户ID、IP地址等
        return DEBOUNCE_PREFIX + keyPrefix + ":";
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