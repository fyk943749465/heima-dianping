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

    /**
     * 将空值的缓存时间设置的比较短一些
     */
    public static final Long CACHE_NULL_TTL = 2L;

    /**
     * 商铺缓存的过期时间
     */
    public static final Long CACHE_SHOP_TTL = 30L;
    /**
     * 商铺缓存前缀
     */
    public static final String CACHE_SHOP_KEY = "cache:shop:";


    public static final Long CACHE_SHOP_TYPE_TTL = 30L;
    /**
     * 商铺类型缓存前缀
     */
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop_type";

    /**
     * 商铺锁前缀
     */
    public static final String SHOP_LOCK_PRFIX = "lock:shop:";


    /**
     * 秒杀业务的库存key前缀
     */
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
}
