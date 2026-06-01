package com.trongtin.asyncprocessingsys.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);

        // Sử dụng StringRedisSerializer để tuần tự hóa và giải tuần tự hóa các giá trị khóa redis
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);

        // Khóa Hash cũng sử dụng phương thức tuần tự hóa StringRedisSerializer.
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    // ── StreamMessageListenerContainer ─────────────────────────
    // Đây là "engine" lắng nghe stream liên tục
    // Giống như một thread chạy ngầm, cứ có message mới là xử lý
    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>>
    streamListenerContainer(RedisConnectionFactory factory) {
                var options =
                StreamMessageListenerContainer
                        .StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        // Chờ tối đa 2s nếu stream rỗng (blocking read)
                        // Hiệu quả hơn poll mỗi 2s vì không loop liên tục
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>>
                container = StreamMessageListenerContainer.create(factory, options);

        container.start();
        // Bắt đầu lắng nghe — subscription sẽ được thêm vào bởi StreamConfig
        log.info("[RedisConfig] StreamListenerContainer started");
        return container;
    }
}