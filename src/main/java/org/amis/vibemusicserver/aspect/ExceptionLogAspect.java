package org.amis.vibemusicserver.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author : KwokChichung
 * @description : 异常日志切面类
 * @createDate : 2026/1/4 5:06
 */
@Aspect
@Component
@Slf4j
public class ExceptionLogAspect {

    // 使用@Around注解定义环绕通知，拦截org.amis.vibemusicserver包及其子包下所有类的所有方法
    @Around("execution(* org.amis.vibemusicserver..*.*(..))")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 正常流程：调用被拦截的方法并返回结果
            return joinPoint.proceed();
        } catch (Throwable t) {
            // 异常处理流程：当被拦截方法抛出异常时执行
            if (joinPoint.getSignature() != null) {
                // 记录错误日志，包含方法签名、参数列表和异常信息
                log.error("Exception in method: {} with args: {}",
                        joinPoint.getSignature().toShortString(),  // 获取方法简短签名
                        joinPoint.getArgs() != null ? Arrays.toString(joinPoint.getArgs()) : "null",  // 获取方法参数，若为null则显示"null"
                        t);  // 异常对象
            }
            // 重新抛出捕获的异常，保持原有异常传播行为
            throw t;
        }
    }

}