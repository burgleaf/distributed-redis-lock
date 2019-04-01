package me.binf.distributed.config;

import me.binf.distributed.lock.RedisLock;
import me.binf.distributed.properties.RedisLockProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;


@Configuration
@ComponentScan({"me.binf.distributed.aop", "me.binf.distributed.properties"})
//是否有 redis 配置的校验，如果没有配置则不会加载改配置，也就是当前插件并不会生效
@Conditional(CheckReqCondition.class)
public class RedisLockConfig {


    private Logger logger = LoggerFactory.getLogger(RedisLockConfig.class);

    @Autowired
    private RedisLockProperties redisLockProperties;



    @Bean
    public RedisLock build(JedisConnectionFactory jedisConnectionFactory) {
        String prefix = redisLockProperties.getLockPrefix();
        Integer sleepTime = redisLockProperties.getSleepTime();

        RedisLock.Builder builder = null;
        builder = new RedisLock.Builder(jedisConnectionFactory);

        if (prefix != null) {
            builder = builder.lockPrefix(prefix);
        }
        if (sleepTime != null) {
            builder = builder.sleepTime(sleepTime);
        }
        RedisLock redisLock = builder.build();

        return redisLock;
    }


}
