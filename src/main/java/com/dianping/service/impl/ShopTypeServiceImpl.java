package com.dianping.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dianping.dto.Result;
import com.dianping.entity.ShopType;
import com.dianping.mapper.ShopTypeMapper;
import com.dianping.service.IShopTypeService;
import com.dianping.util.RedisConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Result queryShoptypes()  {
        // 1. 查询 redis 中是否有缓存

        String key = RedisConstants.CACHE_SHOP_TYPE_KEY;
        String jsonShopTypes = redisTemplate.opsForValue().get(key);
        if (jsonShopTypes != null) {

            JSONArray shopTypeArray = JSONUtil.parseArray(jsonShopTypes);
            List<ShopType> shopTypes = shopTypeArray.toList(ShopType.class);
            return Result.ok(shopTypes);
        }
        // 2. 缓存不存在，查询数据库
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        if (shopTypes.isEmpty()) {
            return Result.fail("shop type is not exist");
        }
        // 3. 缓存到 redis 中
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopTypes), RedisConstants.CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
        return Result.ok(shopTypes);
    }
}
