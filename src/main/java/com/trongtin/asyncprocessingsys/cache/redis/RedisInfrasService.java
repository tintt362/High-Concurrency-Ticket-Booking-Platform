package com.trongtin.asyncprocessingsys.cache.redis;


import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public interface RedisInfrasService {
    void setString(String key, String value);
    String getString(String key);

    void setObject(String key, Object value);
    <T> T getObject(String key, Class<T> targetClass);

//    void put(String key, Object value, long timeout, TimeUnit unit);
//
//    void put(String key, Object value, long expireTime);

    // delete redis by key
    void delete(String key);
    RedisTemplate<String, Object> getRedisTemplate(); // Thêm phương thức này
    // int
    void setInt(String key, int value);
    int getInt(String key);

    // bit

}
