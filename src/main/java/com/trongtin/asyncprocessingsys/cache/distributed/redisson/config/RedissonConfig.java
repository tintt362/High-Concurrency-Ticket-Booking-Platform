package com.trongtin.asyncprocessingsys.cache.distributed.redisson.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RedissonConfig {

//        @Value("redis://127.0.0.1:6399")
//        private String redisAddress;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6319").setConnectionPoolSize(50).setDatabase(0);

        return Redisson.create(config);
    }

}
