package com.dianping.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.dianping.dto.UserDTO;
import com.dianping.util.RedisConstants;
import com.dianping.util.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 该拦截器不拦截用户请求，只是对 token 进行续期操作。
 */
public class RefreshTokenIntercepter implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenIntercepter(StringRedisTemplate redisTemplate) {
        stringRedisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 获取 请求头中的 token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return true;   // 不做拦截，请求放行
        }
        String key = RedisConstants.LOGIN_USER_KEY + token;
        // 2. 获取 redis 中的用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        // 3. 判断用户是否存在
        if (userMap.isEmpty()) {
           return true;   // 不做拦截，放行
        }
        //4. 将查询到的 Hash 数据，转换为 UserDTO 对象
        UserDTO user = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        // 5. 存在，保存用户信息到 ThreadLocal中
        UserHolder.saveUser(user);
        // 6. 刷新 token 有效期
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 7. 放行
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
