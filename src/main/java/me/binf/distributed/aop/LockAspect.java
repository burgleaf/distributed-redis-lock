package me.binf.distributed.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Aspect
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class LockAspect {



    @Pointcut("@annotation(me.binf.distributed.annotations.Lock)")
    public void doLock() {
    }


    @Around("doLock()")
    public void around(ProceedingJoinPoint jp) {
        try {
            //lock
            jp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }finally {
            //unlock
        }

    }


}
