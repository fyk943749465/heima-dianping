package com.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dianping.dto.Result;
import com.dianping.entity.ShopType;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

public interface IShopTypeService extends IService<ShopType> {

    Result queryShoptypes() ;
}
