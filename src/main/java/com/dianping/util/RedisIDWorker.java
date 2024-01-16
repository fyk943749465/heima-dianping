package com.dianping.util;


import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIDWorker {

    private static final long BEGIN_TIME = 1640995200L;

    private static final int COUNT = 32;

    private StringRedisTemplate redisTemplate;

    public RedisIDWorker(StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = stringRedisTemplate;
    }

    public long nextId(String prefix) {
        // 1.生成时间戳
        long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIME;
        // 2.生成序列号
        // 2.1获得天
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = redisTemplate.opsForValue().increment("icr:" + prefix + ":" + date);
        //3.拼接返回
        return (timestamp << COUNT) + count;
    }
}
