package com.trongtin.asyncprocessingsys.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportAnalyzerAI {

    private final OllamaService ollamaService;

    private static final String ANALYZE_SYSTEM_PROMPT = """
            Bạn là chuyên gia phân tích dữ liệu kinh doanh cho SaaS.
            
            Nhiệm vụ: Tóm tắt báo cáo thành executive summary ngắn gọn.
            
            Quy tắc:
            - Tối đa 5 điểm chính, mỗi điểm 1-2 câu
            - Nêu rõ xu hướng tăng/giảm nếu có số liệu
            - Format: dùng ký tự • cho mỗi điểm
            - Ngôn ngữ: tiếng Việt, chuyên nghiệp
            """;

    private static final String INSIGHTS_SYSTEM_PROMPT = """
            Bạn là tư vấn kinh doanh cho SaaS.
            
            Nhiệm vụ: Đề xuất hành động cụ thể dựa trên dữ liệu.
            
            Quy tắc:
            - Đúng 3 hành động, mỗi hành động 1 câu
            - Hành động phải cụ thể, thực hiện được ngay
            - Format: đánh số 1. 2. 3.
            - Ngôn ngữ: tiếng Việt
            """;

    private static final String ANOMALY_SYSTEM_PROMPT = """
            Bạn là hệ thống phát hiện bất thường trong dữ liệu kinh doanh.
            
            Quy tắc:
            - Chỉ nêu điểm BẤT THƯỜNG đáng lo ngại
            - Nếu không có gì bất thường: trả về "Không phát hiện bất thường."
            - Tối đa 3 điểm, ngắn gọn
            """;

    // ─────────────────────────────────────────
    // Tóm tắt báo cáo → lưu vào job.aiSummary
    // ─────────────────────────────────────────
    public String analyze(String reportData, String reportType) {
        if (reportData == null || reportData.isBlank()) return null;

        String userMessage = """
                Loại báo cáo: %s
                Dữ liệu:
                %s
                
                Tạo executive summary:
                """.formatted(reportType, truncate(reportData, 2000));
        // truncate: tránh gửi quá nhiều data → AI chậm hoặc lỗi

        log.info("[ReportAnalyzerAI] Analyzing | type={}", reportType);

        String result = ollamaService.chat(ANALYZE_SYSTEM_PROMPT, userMessage);

        if (result == null) {
            return "Không thể tạo tóm tắt tự động. Xem báo cáo đầy đủ.";
            // Fallback message — không crash, không null
        }

        return result;
    }

    // ─────────────────────────────────────────
    // Đề xuất hành động → lưu vào job.aiInsights
    // ─────────────────────────────────────────
    public String generateInsights(String reportData, String reportType) {
        if (reportData == null || reportData.isBlank()) return null;

        String userMessage = """
                Loại báo cáo: %s
                Dữ liệu:
                %s
                
                Đề xuất 3 hành động cụ thể:
                """.formatted(reportType, truncate(reportData, 2000));

        log.info("[ReportAnalyzerAI] Generating insights | type={}", reportType);

        String result = ollamaService.chat(INSIGHTS_SYSTEM_PROMPT, userMessage);

        return result != null ? result
                : "Không thể tạo đề xuất tự động.";
    }

    // ─────────────────────────────────────────
    // Phát hiện bất thường
    // ─────────────────────────────────────────
    public String detectAnomalies(String data) {
        if (data == null) return null;

        return ollamaService.chat(ANOMALY_SYSTEM_PROMPT,
                "Phân tích dữ liệu:\n" + truncate(data, 1500));
    }

    // ─────────────────────────────────────────
    // Helper: giới hạn độ dài text gửi cho AI
    // Tránh gửi quá nhiều → AI chậm, tốn memory
    // ─────────────────────────────────────────
    private String truncate(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) return text;
        return text.substring(0, maxChars) + "...[truncated]";
    }
}