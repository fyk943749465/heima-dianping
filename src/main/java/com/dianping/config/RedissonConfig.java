package com.dianping.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {

        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.10.17:6379").setPassword("thinker");
        // 创建 RedissonClient 对象
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redissonClient1() {

        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.10.17:6379").setPassword("thinker");
        // 创建 RedissonClient 对象
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redissonClient2() {

        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.10.17:6379").setPassword("thinker");
        // 创建 RedissonClient 对象
        return Redisson.create(config);
    }
}
