package me.binf.distributed.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Slf4j
public class CheckReqCondition implements Condition {


    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        try {
            RedisConnectionFactory jedisConnectionFactory = conditionContext.getBeanFactory().getBean(RedisConnectionFactory.class);
            return true;
        }catch (Exception e){
            log.debug("redis-lock not valid ",e);
            return true;
        }

    }
}
