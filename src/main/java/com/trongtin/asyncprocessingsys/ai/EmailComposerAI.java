package com.trongtin.asyncprocessingsys.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailComposerAI {

    private final OllamaService ollamaService;

    // System prompt định nghĩa "tính cách" của AI
    // Viết một lần, dùng cho mọi request
    private static final String COMPOSE_SYSTEM_PROMPT = """
            Bạn là chuyên gia viết email chuyên nghiệp cho phần mềm SaaS.
            
            Nhiệm vụ: Viết nội dung email HTML dựa trên thông tin được cung cấp.
            
            Quy tắc BẮT BUỘC:
            1. Chỉ trả về HTML body content
            2. KHÔNG có <!DOCTYPE>, <html>, <head>, <body>
            3. Tone: chuyên nghiệp nhưng thân thiện
            4. Có lời chào cá nhân hóa nếu biết tên người nhận
            5. Có call-to-action rõ ràng ở cuối
            6. Ngôn ngữ: tiếng Việt
            7. KHÔNG giải thích, KHÔNG thêm text ngoài HTML
            """;

    private static final String SUBJECT_SYSTEM_PROMPT = """
            Bạn là chuyên gia email marketing.
            Nhiệm vụ: Đề xuất subject line email tốt hơn.
            Quy tắc: Chỉ trả về 1 subject line, dưới 60 ký tự, không giải thích.
            """;

    // ─────────────────────────────────────────
    // Sinh HTML email từ context ngắn
    //
    // context: mô tả mục đích email
    //   Ví dụ: "Chào mừng user mới đăng ký, nhắc xác thực email"
    //
    // recipientName: tên người nhận để AI xưng hô đúng
    //
    // Return: HTML string, hoặc null nếu AI fail
    // ─────────────────────────────────────────
    public String generateBody(String context, String recipientName) {
        if (context == null || context.isBlank()) return null;

        String userMessage = """
                Viết email HTML với thông tin:
                - Người nhận: %s
                - Nội dung cần truyền đạt: %s
                
                Trả về HTML body ngay, không giải thích.
                """.formatted(
                recipientName != null ? recipientName : "Khách hàng",
                context
        );

        log.info("[EmailComposerAI] Generating body | recipient={}",
                recipientName);

        String result = ollamaService.chat(COMPOSE_SYSTEM_PROMPT, userMessage);

        if (result == null) {
            log.warn("[EmailComposerAI] AI fail → caller will use original body");
        } else {
            log.info("[EmailComposerAI] Generated | length={}", result.length());
        }

        return result;
    }

    // ─────────────────────────────────────────
    // Gợi ý subject line tốt hơn
    // Return: subject mới, hoặc null nếu AI fail
    // ─────────────────────────────────────────
    public String suggestSubject(String originalSubject, String context) {
        if (originalSubject == null) return null;

        String userMessage = "Subject gốc: %s\nContext: %s\nĐề xuất:"
                .formatted(originalSubject, context);

        return ollamaService.chat(SUBJECT_SYSTEM_PROMPT, userMessage);
    }
}