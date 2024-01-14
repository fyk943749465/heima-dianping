package com.dianping.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dianping.dto.LoginFormDTO;
import com.dianping.dto.Result;
import com.dianping.dto.UserDTO;
import com.dianping.util.RegexUtils;
import com.dianping.util.SystemConstants;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.dianping.mapper.UserMapper;
import com.dianping.entity.User;
import com.dianping.service.IUserService;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. 手机号不合法，返回错误信息
            return Result.fail("手机号格式错误");
        }

        // 3. 手机号合法，生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4. 保存验证码到 session 中
        session.setAttribute("code", code);
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
        // 2. 校验验证码（从 session 中 取出验证码）
        Object cacheCode = session.getAttribute("code");
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.toString().equals(code)) {
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
        // 7. 保存用户信息到 session 当中
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
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
