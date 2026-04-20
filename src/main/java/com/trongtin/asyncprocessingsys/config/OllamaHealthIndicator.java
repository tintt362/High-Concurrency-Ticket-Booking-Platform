package com.trongtin.asyncprocessingsys.config;

import com.trongtin.asyncprocessingsys.ai.OllamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

// Tên "ollama" sẽ xuất hiện trong /actuator/health response
@Component("ollama")
@RequiredArgsConstructor
public class OllamaHealthIndicator implements HealthIndicator {

    private final OllamaService ollamaService;

    @Override
    public Health health() {
        if (ollamaService.isAvailable()) {
            return Health.up()
                    .withDetail("status", "Ollama is running")
                    .withDetail("info", "AI features are active")
                    .build();
        }
        // DOWN không làm app fail — app vẫn healthy
        // Chỉ là AI features bị tắt (dùng fallback)
        return Health.down()
                .withDetail("status", "Ollama not available")
                .withDetail("info", "AI features disabled — using fallback")
                .build();
    }
}