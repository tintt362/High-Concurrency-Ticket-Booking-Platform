package com.trongtin.asyncprocessingsys.controller;

import com.trongtin.asyncprocessingsys.ai.ChatbotService;
import com.trongtin.asyncprocessingsys.dto.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chatbot")
@Slf4j
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    // POST /api/v1/chatbot/ask
    // Body: { "question": "Có bao nhiêu job đang FAILED?" }
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ChatResponse>> ask(
            @Valid @RequestBody ChatRequest request) {

        log.info("[ChatbotController] Question: {}", request.getQuestion());
        String answer = chatbotService.chat(request.getQuestion());

        return ResponseEntity.ok(
                ApiResponse.ok(new ChatResponse(answer)));
    }

    // ── Inner classes dùng cho request/response ──────────
    @Data
    static class ChatRequest {
        @NotBlank(message = "Question is required")
        @Size(max = 500, message = "Question too long (max 500 chars)")
        private String question;
    }

    @Data
    static class ChatResponse {
        private final String answer;
    }
}