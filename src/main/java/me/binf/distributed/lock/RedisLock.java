package me.binf.distributed.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.Collections;

public class RedisLock {

    private static final String LOCK_MSG = "OK";
    private static final Long UNLOCK_MSG = 1L;
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    /**
     * time millisecond
     */
    private static final int TIME = 1000;
    private static Logger logger = LoggerFactory.getLogger(RedisLock.class);
    private String lockPrefix;
    private int sleepTime;
    private JedisConnectionFactory jedisConnectionFactory;
    private String type;


    private RedisLock(Builder builder) {
        this.jedisConnectionFactory = builder.jedisConnectionFactory;
        this.type = builder.type;
        this.lockPrefix = builder.lockPrefix;
        this.sleepTime = builder.sleepTime;
    }

    /**
     * get Redis connection
     *
     * @return
     */
    private Object getConnection() {
        Object connection;
        if (ConnectionType.SINGLE.name().equals(type)) {
            RedisConnection redisConnection = jedisConnectionFactory.getConnection();
            connection = redisConnection.getNativeConnection();
        } else {
            RedisClusterConnection clusterConnection = jedisConnectionFactory.getClusterConnection();
            connection = clusterConnection.getNativeConnection();
        }
        return connection;
    }



    /**
     * blocking lock
     * @param key
     * @param request
     */
    public void lock(String key, String request) throws InterruptedException {
        Object connection = getConnection();
        for (; ; ) {
            if (tryLock(key, request, 10 * TIME, connection)) {
                closeConnection(connection);
                break;
            }
            Thread.sleep(sleepTime);
        }
    }

    /**
     * blocking lock,custom time
     *
     * @param key
     * @param request
     * @param blockTime custom time
     * @return
     * @throws InterruptedException
     */
    public boolean lock(String key, String request, int blockTime) throws InterruptedException {
        Object connection = getConnection();
        while (blockTime >= 0) {
            if (tryLock(key, request, 10 * TIME, connection)) {
                closeConnection(connection);
                return true;
            }
            blockTime -= sleepTime;
            Thread.sleep(sleepTime);
        }
        return false;
    }


    /**
     * Non-blocking lock
     * @param key     lock business type
     * @param request value
     * @return true lock success
     * false lock fail
     */
    public boolean tryLock(String key, String request) {
        //get connection
        Object connection = getConnection();
        boolean res = tryLock(key, request, 10 * TIME, connection);
        closeConnection(connection);
        return res;
    }

    /**
     * Non-blocking lock
     * @param key        lock business type
     * @param request    value
     * @param expireTime custom expireTime
     * @return true lock success
     * false lock fail
     */
    public boolean tryLock(String key, String request, int expireTime) {
        //get connection
        Object connection = getConnection();
        boolean res = tryLock(key, request, expireTime, connection);
        closeConnection(connection);
        return res;
    }

    /**
     * base try lock
     * @param key
     * @param request
     * @param expireTime
     * @param connection
     * @return
     */
    private boolean tryLock(String key, String request, int expireTime, Object connection) {
        String result;
        if (connection instanceof Jedis) {
            result = ((Jedis) connection).set(lockPrefix + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        } else {
            result = ((JedisCluster) connection).set(lockPrefix + key, request, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        }
        if (LOCK_MSG.equals(result)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 关闭连接
     *
     * @param connection
     */
    private void closeConnection(Object connection) {
        if (connection instanceof Jedis) {
            ((Jedis) connection).close();
        }
    }

    /**
     * unlock
     *
     * @param key
     * @param request request must be the same as lock request
     * @return
     */
    public boolean unlock(String key, String request) {
        //get connection
        Object connection = getConnection();
        //lua script
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = null;
        if (connection instanceof Jedis) {
            result = ((Jedis) connection).eval(script, Collections.singletonList(lockPrefix + key), Collections.singletonList(request));
            ((Jedis) connection).close();
        } else if (connection instanceof JedisCluster) {
            result = ((JedisCluster) connection).eval(script, Collections.singletonList(lockPrefix + key), Collections.singletonList(request));
        } else {
            //throw new RuntimeException("instance is error") ;
            return false;
        }
        if (UNLOCK_MSG.equals(result)) {
            return true;
        } else {
            return false;
        }
    }


    public enum ConnectionType {
        SINGLE, CLUSTER
    }

    public static class Builder {
        private static final String DEFAULT_LOCK_PREFIX = "LOCK_";
        /**
         * default sleep time
         */
        private static final int DEFAULT_SLEEP_TIME = 100;

        private JedisConnectionFactory jedisConnectionFactory = null;

        private String type;

        private String lockPrefix = DEFAULT_LOCK_PREFIX;
        private int sleepTime = DEFAULT_SLEEP_TIME;

        public Builder(JedisConnectionFactory jedisConnectionFactory, String type) {
            this.jedisConnectionFactory = jedisConnectionFactory;
            this.type = type;
        }

        public Builder lockPrefix(String lockPrefix) {
            this.lockPrefix = lockPrefix;
            return this;
        }

        public Builder sleepTime(int sleepTime) {
            this.sleepTime = sleepTime;
            return this;
        }

        public RedisLock build() {
            return new RedisLock(this);
        }

    }
}
