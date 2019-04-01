package me.binf.distributed.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
//定义配置前缀
@ConfigurationProperties(prefix = "binf.redis-lock")
public class RedisLockProperties {

    private String lockPrefix;

    private Integer sleepTime;

    public String getLockPrefix() {
        return lockPrefix;
    }

    public void setLockPrefix(String lockPrefix) {
        this.lockPrefix = lockPrefix;
    }

    public Integer getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(Integer sleepTime) {
        this.sleepTime = sleepTime;
    }
}
