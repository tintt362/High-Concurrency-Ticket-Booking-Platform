package com.trongtin.asyncprocessingsys.cache.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class RedisInfrasServiceImpl implements RedisInfrasService{
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public void setString(String key, String value) {
        if (StringUtils.hasLength(key)) { // null or ''
            return;
        }
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public String getString(String key) {
//        Object result = redisTemplate.opsForValue().get(key);
//        if (result == null) {
//            return null;
//        }
//        return String.valueOf(result);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .map(String::valueOf)
                .orElse(null);
    }

    @Override
    public void setObject(String key, Object value) {
//        log.info("Set redis::1, {}", key);
        if (!StringUtils.hasLength(key)) { // null or ''
//            log.info("Set redis::null, {}", StringUtils.hasLength(key));
            return;
        }

        try {
            redisTemplate.opsForValue().set(key, value);
        }catch (Exception e){
            log.error("setObject error: key={}, error={}", key, e.getMessage(), e);
        }
//        redisTemplate.opsForValue().set(key, value);
//        // Kiểm tra xem giá trị có được lưu thành công hay không
//        Object result = redisTemplate.opsForValue().get(key);
//        log.info("Set redis::{}", result != null && result.equals(value));
    }
    @Override
    public <T> T getObject(String key, Class<T> targetClass) {
        Object result = redisTemplate.opsForValue().get(key);

        if (result == null) {
            return null;
        }

        // Nếu Spring Redis đã tự động phục hồi đúng kiểu Object mong muốn
        if (targetClass.isInstance(result)) {
            return targetClass.cast(result);
        }

        // Trường hợp dự phòng: Nếu cấu hình Redis thay đổi dẫn đến trả về LinkedHashMap
        if (result instanceof Map) {
            try {
                // Tái sử dụng ObjectMapper được cấu hình chuẩn (hoặc tự inject ObjectMapper của Spring vào)
                ObjectMapper objectMapper = new ObjectMapper();
// Sửa lại dòng code số 75 trong ảnh thành:
                objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());                return objectMapper.convertValue(result, targetClass);
            } catch (IllegalArgumentException e) {
                log.error("Lỗi convert LinkedHashMap sang Object: {}", e.getMessage());
                return null;
            }
        }

        return null;
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    @Override
    public void setInt(String key, int value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public int getInt(String key) {
        return (int) redisTemplate.opsForValue().get(key);
    }
}