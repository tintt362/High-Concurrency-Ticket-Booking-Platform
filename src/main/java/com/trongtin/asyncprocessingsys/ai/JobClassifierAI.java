package com.trongtin.asyncprocessingsys.ai;

import com.trongtin.asyncprocessingsys.model.enums.JobPriority;
import com.trongtin.asyncprocessingsys.model.enums.JobType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobClassifierAI {

    private final OllamaService ollamaService;

    private static final String SYSTEM_PROMPT = """
            Bạn là hệ thống phân loại độ ưu tiên xử lý công việc.
            
            Phân loại:
            HIGH   = Khẩn cấp: thanh toán, bảo mật, lỗi hệ thống, cảnh báo quan trọng
            MEDIUM = Bình thường: thông báo, xác nhận, báo cáo định kỳ
            LOW    = Không gấp: marketing, newsletter, quảng cáo
            
            Quy tắc: Chỉ trả về MỘT từ: HIGH hoặc MEDIUM hoặc LOW
            Không giải thích, không thêm gì khác.
            """;

    // Phân loại priority từ nội dung job
    // Nếu AI fail → trả MEDIUM
    // Không bao giờ để AI failure làm job mất đi
    public JobPriority classify(String payload, JobType type) {
        if (payload == null) return JobPriority.MEDIUM;

        String userMessage = """
                Loại job: %s
                Nội dung: %s
                
                Priority là gì?
                """.formatted(type.name(), payload);

        log.info("[JobClassifierAI] Classifying | type={}", type);

        String result = ollamaService.chat(SYSTEM_PROMPT, userMessage);

        // AI fail → default MEDIUM
        if (result == null) {
            log.warn("[JobClassifierAI] AI fail → default MEDIUM");
            return JobPriority.MEDIUM;
        }

        // Parse kết quả — AI đôi khi trả thêm text thừa
        // Dùng contains thay vì equals để an toàn hơn
        String cleaned = result.toUpperCase().trim();

        if (cleaned.contains("HIGH"))   return JobPriority.HIGH;
        if (cleaned.contains("LOW"))    return JobPriority.LOW;
        return JobPriority.MEDIUM;
        // Mặc định MEDIUM nếu AI trả về thứ không rõ ràng
    }
}