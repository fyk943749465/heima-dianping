package com.dianping.util;

public class RedisConstants {

    /**
     * redis 中保存的验证码 key 前缀
     */
    public static final String LOGIN_CODE_KEY = "login:code:";
    /**
     * redis 保存的验证码的时间
     */
    public static final Long LOGIN_CODE_TTL = 2L;

    /**
     * redis 存储的 token 的前缀
     */
    public static final String LOGIN_USER_KEY = "login:token:";
    /**
     * redis 中 token 的有效期
     */
    public static final Long LOGIN_USER_TTL = 30L;
}
