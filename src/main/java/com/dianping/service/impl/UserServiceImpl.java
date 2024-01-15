package com.dianping.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dianping.dto.LoginFormDTO;
import com.dianping.dto.Result;
import com.dianping.dto.UserDTO;
import com.dianping.entity.User;
import com.dianping.mapper.UserMapper;
import com.dianping.service.IUserService;
import com.dianping.util.RedisConstants;
import com.dianping.util.RegexUtils;
import com.dianping.util.SystemConstants;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. 手机号不合法，返回错误信息
            return Result.fail("手机号格式错误");
        }

        // 3. 手机号合法，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4. 保存验证码到 redis 中，并设置有效期
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 5. 发送验证码，调用第三方平台的服务，发送验证码，这里不是重点，模拟一下
        log.info("发送短信验证码成功，验证码:{}", code);
        // 6. 返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1. 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. 手机号不合法，返回错误信息
            return Result.fail("手机号格式错误");
        }
        // 2. 校验验证码（从 redis 中 取出验证码）
        // Object cacheCode = session.getAttribute("code");
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 3. 不一致，报错
            return Result.fail("验证码错误");
        }

        // 4. 一致，根据手机号查询用户
        User user = query().eq("phone", phone).one();
        // 5. 判断用户是否存在
        if (user == null) {
            // 6. 不存在，创建全新用户
            user = createUserWithPhone(phone);
        }
        // 7. 保存用户信息到 redis 当中
        // 7.1 生成随机 token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        // 7.2 将 user 对象转换为 Hash
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 7.3 存储在 redis 中
        String tokeKey = RedisConstants.LOGIN_USER_KEY + token;
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,
                new HashMap<>(), // 创建一个 HashMap 结构
                CopyOptions.create()
                        .setIgnoreNullValue(true)  // 忽略 null 值
                        .setFieldValueEditor((fileName, fileValue) -> fileValue.toString()) // 设置字段值的修改器
        );
        // 这里会报转换异常，因为userDTO中id字段为long类型，而stringRedisTemplate只能处理String类型
        stringRedisTemplate.opsForHash().putAll(tokeKey, userMap);
        // 7.4. 设置 token 的有效期
        // token 有效期是 30 分钟，如果30分钟没有用户请求，session 过期，但是如果有请求，session 应该一直续期
        stringRedisTemplate.expire(tokeKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 登录成功，删除验证码
        stringRedisTemplate.opsForValue().getOperations().delete(RedisConstants.LOGIN_CODE_KEY + phone);
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        // 1. 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
