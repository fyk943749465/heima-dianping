package com.dianping.config;

import com.dianping.dto.UserDTO;
import com.dianping.entity.User;
import com.dianping.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 获取 session
        HttpSession session = request.getSession();
        // 2. 获取 session 中的用户
        Object user = session.getAttribute("user");
        // 3. 判断用户是否存在
        if (user == null) {
            // 4. 不存在，拦截
            response.setStatus(401);
            return false;
        }
        // 5. 存在，保存用户信息到 ThreadLocal中
        UserHolder.saveUser((UserDTO) user);
        // 6. 放行
        return true;
    }


    /**
     * 请求完成之后，移除 user，避免内存泄露
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        // 移除用户
        UserHolder.removeUser();
    }
}
