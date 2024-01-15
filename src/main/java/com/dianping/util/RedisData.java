package com.dianping.util;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RedisData {

    private LocalDateTime expireTime;  // 逻辑过期时间，不是真正设置在redis中的过期时间，是为了解决redis缓存击穿问题考虑的
    private Object data;
}
