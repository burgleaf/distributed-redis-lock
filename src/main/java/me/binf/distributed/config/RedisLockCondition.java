package me.binf.distributed.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.StringUtils;

@Slf4j
public class RedisLockCondition implements Condition {



    private static Logger logger = LoggerFactory.getLogger(RedisLockCondition.class);

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        //如果没有加入redis配置的就返回false
        String property = conditionContext.getEnvironment().getProperty("spring.redis.host");
        String clusterProperty = conditionContext.getEnvironment().getProperty("spring.redis.cluster.nodes");
        if (StringUtils.isEmpty(property) && StringUtils.isEmpty(clusterProperty)){
            logger.warn("Need to configure redis!");
            return false ;
        }else {
            return true;
        }
    }
}
