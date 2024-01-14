package com.dianping.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()) // 增加拦截器
                .excludePathPatterns(
                  "/user/code",
                  "/user/login"
                );  // 排除的路径，根据业务需求，不需要进行用户验证的路径
    }
}
