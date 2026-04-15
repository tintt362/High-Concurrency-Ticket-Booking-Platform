package com.trongtin.asyncprocessingsys.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
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
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer s = new StringRedisSerializer();
        template.setKeySerializer(s);
        template.setValueSerializer(s);
        template.setHashKeySerializer(s);
        template.setHashValueSerializer(s);
        template.afterPropertiesSet();
        return template;
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