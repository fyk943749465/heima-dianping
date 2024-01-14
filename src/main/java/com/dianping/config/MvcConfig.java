package com.dianping.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 这两个拦截器，需要配置先后顺序
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new RefreshTokenIntercepter(stringRedisTemplate))
                .addPathPatterns("/**")  // 拦截所有请求，也是默认的配置
                .order(0);

        registry.addInterceptor(new LoginInterceptor()) // 增加拦截器
                .excludePathPatterns(
                        "/shop/**",
                        "/shop-type/**",
                        "/user/code",
                        "/user/login"
                ).order(1);  // 排除的路径，根据业务需求，不需要进行用户验证的路径
    }
}
