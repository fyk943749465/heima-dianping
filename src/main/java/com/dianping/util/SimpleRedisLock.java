package com.dianping.util;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock{

    private static final String KEY_PRIFIX = "lock:";
    private String name; // 锁名字
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 释放分布式锁的lua脚本，这样做是为了保证原子性
     */
    private static final DefaultRedisScript<Long>  UNLOCK_SCRIPT;

    /**
     * 初始化 unlock.lua 脚本
     */
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        // 设置脚本的位置 -- classpath 下的 unlock.lua 文件
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        // 返回类型（与脚本的返回类型一致）
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    // 使用UUID，是分布式情况下，避免生成相同的值
    // 因为分布式情况下，相同服务的不同实例的线程id是有可能相同的
    private static final String ID_PREFIX = UUID.fastUUID().toString(true) + "-";

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();

        // 获取锁
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PRIFIX + name, threadId, timeoutSec, TimeUnit.MILLISECONDS);
        // 有自动拆箱，就有可能返回null，做好空指针判断
        return Boolean.TRUE.equals(success);
    }

    /**
     * 使用 lua 脚本，删除锁，保证操作的原子性
     *
     */
    @Override
    public void unlock() {
        // 这个是原子性的
        stringRedisTemplate.execute(UNLOCK_SCRIPT,                // 脚本
                Collections.singletonList(KEY_PRIFIX + name),     // 锁的key
                ID_PREFIX + Thread.currentThread().getId() // 当前线程标识
                );
    }

    /**
     * 只要释放锁的操作不是原子性的，那么并发访问就会有问题。
     * 所以，不这样做。
     */
    //@Override
    public void unlock2() {
        // 获取线程标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁中的标识
        String id = stringRedisTemplate.opsForValue().get(KEY_PRIFIX + name);
        // 判断标识是否一致
        // 一致
        if (threadId.equals(id)) {
            // 释放锁
            stringRedisTemplate.delete(KEY_PRIFIX + name);
        }
        // 不一致，不做处理。这种情况发生在，业务处理时间长，超时，锁自动释放了
    }
}
