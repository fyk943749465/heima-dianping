package com.dianping.config;

import com.dianping.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 拦截需要登录的请求，做验证
 */
public class LoginInterceptor implements HandlerInterceptor {


    /**
     * 请求之前拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 判断是否需要拦截请求（ThreadLocal 中是否有该用户）
        if (UserHolder.getUser() == null) {
            response.setStatus(401);
            // 拦截请求
            return false;
        }
        // 2. 放行
        return true;
    }
}
