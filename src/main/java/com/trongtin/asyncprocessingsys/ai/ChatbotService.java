package com.trongtin.asyncprocessingsys.ai;

import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {

    private final OllamaService ollamaService;
    private final JobRepository jobRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String SYSTEM_PROMPT = """
            Bạn là AI assistant hỗ trợ nội bộ cho hệ thống Async Job Processing.
            
            Bạn có thể trả lời về:
            - Trạng thái job (PENDING, PROCESSING, DONE, FAILED)
            - Thống kê hệ thống (số lượng job theo từng trạng thái)
            - Queue size hiện tại
            - Hướng dẫn sử dụng API
            
            Quy tắc:
            - Luôn trả lời bằng tiếng Việt
            - Ngắn gọn, chính xác, dễ hiểu
            - Nếu không chắc → nói rõ không chắc, không bịa số liệu
            """;

    // ─────────────────────────────────────────
    // Trả lời câu hỏi bằng ngôn ngữ tự nhiên
    //
    // Luồng:
    // 1. Lấy data thật từ DB và Redis
    // 2. Ghép data vào system prompt
    // 3. AI dùng data đó để trả lời
    //
    // Kết quả: AI trả lời chính xác vì có data thật
    // ─────────────────────────────────────────
    public String chat(String userQuestion) {
        log.info("[Chatbot] Question: {}", userQuestion);

        // Build context thực tế — AI dùng data này để trả lời
        String systemContext = buildSystemContext();
        String fullSystemPrompt = SYSTEM_PROMPT
                + "\n\n--- Dữ liệu hệ thống hiện tại ---\n"
                + systemContext;

        String answer = ollamaService.chat(fullSystemPrompt, userQuestion);

        if (answer == null) {
            return "Xin lỗi, AI assistant tạm thời không khả dụng. "
                    + "Vui lòng thử lại sau hoặc dùng API trực tiếp.";
        }

        log.info("[Chatbot] Answered | length={}", answer.length());
        return answer;
    }

    // ─────────────────────────────────────────
    // Lấy data thật từ DB và Redis
    // AI dùng data này để trả lời chính xác
    // ─────────────────────────────────────────
    private String buildSystemContext() {
        try {
            // Đếm job theo từng trạng thái
            long total      = jobRepository.count();
            long pending    = jobRepository.findByStatus(JobStatus.PENDING).size();
            long processing = jobRepository.findByStatus(JobStatus.PROCESSING).size();
            long done       = jobRepository.findByStatus(JobStatus.DONE).size();
            long failed     = jobRepository.findByStatus(JobStatus.FAILED).size();

            // Lấy queue size từ Redis
            Long highQueue   = safeQueueSize("queue:email:high");
            Long mediumQueue = safeQueueSize("queue:email:medium");
            Long lowQueue    = safeQueueSize("queue:email:low");
            Long dlqSize     = safeQueueSize("queue:dead-letter");

            // 5 job failed gần nhất để AI có thể nhắc đến
            List<String> recentFailedIds = jobRepository
                    .findByStatus(JobStatus.FAILED)
                    .stream()
                    .limit(5)
                    .map(j -> j.getId().toString().substring(0, 8) + "...")
                    .collect(Collectors.toList());

            return """
                    Tổng số job: %d
                    - PENDING: %d
                    - PROCESSING: %d
                    - DONE: %d
                    - FAILED: %d
                    
                    Queue email:
                    - HIGH priority: %d job
                    - MEDIUM priority: %d job
                    - LOW priority: %d job
                    - Dead Letter Queue: %d job
                    
                    Job FAILED gần nhất: %s
                    """.formatted(
                    total, pending, processing, done, failed,
                    highQueue, mediumQueue, lowQueue, dlqSize,
                    recentFailedIds.isEmpty() ? "Không có"
                            : String.join(", ", recentFailedIds)
            );

        } catch (Exception e) {
            // Nếu lấy data fail → vẫn cho AI trả lời
            // AI sẽ nói "không có thông tin" thay vì crash
            log.error("[Chatbot] Cannot build context | error={}", e.getMessage());
            return "Không thể lấy dữ liệu hệ thống hiện tại.";
        }
    }

    // Helper: lấy queue size an toàn — trả 0 nếu lỗi
    private Long safeQueueSize(String queueName) {
        try {
            Long size = redisTemplate.opsForList().size(queueName);
            return size != null ? size : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}