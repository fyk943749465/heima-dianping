package com.dianping;

import com.dianping.service.impl.ShopServiceImpl;
import com.dianping.util.RedisIDWorker;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class RedisDianpingApplicationTests {

    @Test
    void contextLoads() {
    }

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private RedisIDWorker redisIDWorker;

    private ExecutorService executorService = Executors.newFixedThreadPool(300);

    @Test
    void testSaveShop() throws InterruptedException {
        shopService.saveShop2Redis(1L, 5L);
    }


    /**
     * 测试 redis 唯一 id 效率
     * @throws InterruptedException
     */
    @Test
    void testRedisId() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(300);
        Runnable task = () -> {

            for (int i = 0; i < 1000; ++i) {
                long order = redisIDWorker.nextId("order");
            }
            countDownLatch.countDown();
        };

        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; ++i) {
            executorService.submit(task);
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();
        System.out.println("time:" + (end - begin));
    }

}
