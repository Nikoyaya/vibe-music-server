package org.amis.vibemusicserver.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author : KwokChichung
 * @description : 异常日志切面类
 * @createDate : 2026/1/4 5:06
 */
@Aspect
@Component
@Slf4j
public class ExceptionLogAspect {

    @Around("execution(* org.amis.vibemusicserver..*.*(..))")
    public Object logException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("Exception in method: {} with args: {}",
                    joinPoint.getSignature(),
                    joinPoint.getArgs(),
                    e);
            throw e;
        }
    }
}

