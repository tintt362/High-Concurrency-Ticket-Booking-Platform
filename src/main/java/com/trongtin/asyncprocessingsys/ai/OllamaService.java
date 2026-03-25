package com.trongtin.asyncprocessingsys.ai;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OllamaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    @Value("${ollama.enabled:true}")
    private boolean enabled;
    // :true = giá trị mặc định nếu không có trong yml

    // Dùng RestTemplateBuilder để set timeout riêng cho AI
    // AI xử lý lâu hơn REST API thường — cần timeout dài hơn
    public OllamaService(RestTemplateBuilder builder,
                         ObjectMapper objectMapper) {
        this.restTemplate = builder.setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(120))
                // 2 phút — model nhỏ có thể cần 30-60s để trả lời
                .build();
        this.objectMapper = objectMapper;
    }

    // ─────────────────────────────────────────────────────
    // METHOD CHÍNH: Gửi prompt → nhận text response
    //
    // systemPrompt: Định nghĩa "vai trò" của AI
    //   Ví dụ: "Bạn là chuyên gia viết email chuyên nghiệp"
    //
    // userMessage: Câu hỏi / yêu cầu cụ thể
    //   Ví dụ: "Viết email chào mừng cho Nguyễn Văn A"
    //
    // QUAN TRỌNG: Trả về null nếu AI fail
    // Caller PHẢI kiểm tra null và dùng fallback
    // ─────────────────────────────────────────────────────
    public String chat(String systemPrompt, String userMessage) {

        // Nếu AI bị tắt trong config → skip ngay
        if (!enabled) {
            log.debug("[Ollama] Disabled — skipping AI call");
            return null;
        }

        String url = baseUrl + "/api/chat";

        // Build request body theo format Ollama API
        Map<String, Object> body = Map.of(
                "model",   model,
                "stream",  false,
                // stream:false = đợi full response
                // stream:true = nhận từng token (như ChatGPT đang gõ)
                // Dùng false cho đơn giản

                "options", Map.of(
                        "temperature", 0.7,
                        // temperature: 0 = chắc chắn, sát thực tế
                        //              1 = sáng tạo, đôi khi ngẫu nhiên
                        //              0.7 = cân bằng tốt cho business

                        "num_predict", 1024
                        // Số token tối đa AI được phép trả về
                        // 1 token ≈ 0.75 từ tiếng Anh
                ),

                "messages", List.of(
                        Map.of("role", "system",
                                "content", systemPrompt),
                        Map.of("role", "user",
                                "content", userMessage)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            log.info("[Ollama] Sending request | model={} | msgLen={}",
                    model, userMessage.length());

            long startTime = System.currentTimeMillis();

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            long duration = System.currentTimeMillis() - startTime;

            // Parse response
            // Cấu trúc response của Ollama:
            // { "message": { "role": "assistant", "content": "..." } }
            Map<String, Object> message =
                    (Map<String, Object>) response.getBody().get("message");
            String content = (String) message.get("content");

            log.info("[Ollama] Response OK | duration={}ms | len={}",
                    duration, content.length());

            return content.trim();

        } catch (Exception e) {
            // AI fail KHÔNG được crash hệ thống
            // Log warn (không phải error) và trả null
            // Caller dùng fallback để hệ thống chạy tiếp
            log.warn("[Ollama] Request failed — will use fallback | error={}",
                    e.getMessage());
            return null;
        }
    }

    // Kiểm tra Ollama đang chạy không
    // Dùng cho health check và để log khi khởi động
    public boolean isAvailable() {
        if (!enabled) return false;
        try {
            restTemplate.getForEntity(baseUrl + "/api/tags", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}