import me.binf.distributed.lock.RedisLock;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RedisLockTest {


    private static Logger logger = LoggerFactory.getLogger(RedisLockTest.class);
    private static ExecutorService executorServicePool;


    private static RedisLock redisLock;

    private static JedisPool jedisPool;


    public static void main(String[] args) throws InterruptedException {
        RedisLockTest redisLockTest = new RedisLockTest();
        redisLockTest.init();
        initThread();

        for (int i = 0; i < 10; i++) {
            executorServicePool.execute(new Worker(i));
        }

        executorServicePool.shutdown();
        while (!executorServicePool.awaitTermination(1, TimeUnit.SECONDS)) {
            logger.info("worker running");
        }
        logger.info("worker over");

    }

    public static void initThread() {


        executorServicePool = new ThreadPoolExecutor(350, 350, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(200), new ThreadPoolExecutor.AbortPolicy());

    }

    @Before
    public void setBefore() {
        init();

    }

    private void init() {

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(60);
        config.setMaxTotal(60);
        config.setMaxWaitMillis(10000);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        //单机
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
        jedisConnectionFactory.setHostName("127.0.0.1");
        jedisConnectionFactory.setPort(6379);
        jedisConnectionFactory.setPassword("");
        jedisConnectionFactory.setTimeout(100000);
        jedisConnectionFactory.afterPropertiesSet();
        redisLock = new RedisLock.Builder(jedisConnectionFactory, "SINGLE")
                .lockPrefix("lock_")
                .sleepTime(100)
                .build();

    }

    private static class Worker implements Runnable {

        private int index;

        public Worker(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            //测试非阻塞锁
            //boolean limit = redisLock.tryLock("abc", "12345");
            //if (limit) {
            //    logger.info("加锁成功=========");
            //    boolean unlock = redisLock.unlock("abc", "12345");
            //    logger.info("解锁结果===[{}]",unlock);
            //} else {
            //    logger.info("加锁失败");
            //
            //}

            //测试非阻塞锁 + 超时时间
            //boolean limit = redisLock.tryLock("abc", "12345",1000);
            //if (limit) {
            //    logger.info("加锁成功=========");
            //    boolean unlock = redisLock.unlock("abc", "12345");
            //    logger.info("解锁结果===[{}]",unlock);
            //} else {
            //    logger.info("加锁失败");
            //
            //}


            //测试阻塞锁
            try {
                redisLock.lock("abc", "12345");
                logger.info("加锁成功=========");
//                redisLock.unlock("abc","12345") ;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //测试阻塞锁 + 阻塞时间
            //try {
            //    boolean limit = redisLock.lock("abc", "12345", 100);
            //    if (limit) {
            //        logger.info("加锁成功=========");
            //        boolean unlock = redisLock.unlock("abc", "12345");
            //        logger.info("解锁结果===[{}]",unlock);
            //    } else {
            //        logger.info("加锁失败");
            //
            //    }
            //} catch (InterruptedException e) {
            //    e.printStackTrace();
            //}
        }
    }


}
