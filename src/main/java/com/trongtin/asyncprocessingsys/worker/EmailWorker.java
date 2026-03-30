package com.trongtin.asyncprocessingsys.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trongtin.asyncprocessingsys.ai.EmailComposerAI;
import com.trongtin.asyncprocessingsys.dto.request.EmailPayload;
import com.trongtin.asyncprocessingsys.model.Job;
import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.repository.JobRepository;
import com.trongtin.asyncprocessingsys.service.EmailService;
import com.trongtin.asyncprocessingsys.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailWorker {

    private final RedisTemplate<String, String> redisTemplate;
    private final JobRepository jobRepository;
    private final EmailService emailService;
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;
    private final EmailComposerAI emailComposerAI;

    private static final String EMAIL_QUEUE      = "queue:email";
    private static final String DEAD_LETTER_QUEUE = "queue:dead-letter";

    // ─────────────────────────────────────────────────────────
    // Poll queue:email mỗi 2 giây
    //
    // fixedDelay = chờ 2s SAU KHI lần trước xong
    // Khác với fixedRate = chạy mỗi 2s dù lần trước chưa xong
    // fixedDelay an toàn hơn — tránh overlap
    // ─────────────────────────────────────────────────────────
    @Scheduled(fixedDelay = 10000)
    public void pollQueue() {
        // RPOP lấy từ cuối queue (FIFO: LPUSH đầu, RPOP cuối)
        // HIGH được lấy ra trước tiên
        String jobIdStr = redisTemplate.opsForList()
                .rightPop("queue:email:high");

        // Không có HIGH → thử MEDIUM
        if (jobIdStr == null) {
            jobIdStr = redisTemplate.opsForList()
                    .rightPop("queue:email:medium");
        }

        // Không có MEDIUM → thử LOW
        if (jobIdStr == null) {
            jobIdStr = redisTemplate.opsForList()
                    .rightPop("queue:email:low");
        }

        // Tất cả rỗng → không làm gì
        if (jobIdStr == null) return;

        log.info("[EmailWorker] Dequeued | jobId={}", jobIdStr);
        processJob(jobIdStr);
    }

    // ─────────────────────────────────────────────────────────
    // Xử lý job
    //
    // @Retryable sẽ tự retry nếu method ném exception
    //   - maxAttempts = 3: thử tối đa 3 lần
    //   - delay = 2000: lần đầu chờ 2s
    //   - multiplier = 2: mỗi lần sau nhân đôi → 2s, 4s, 8s
    // ─────────────────────────────────────────────────────────
    @Retryable(
            retryFor    = {Exception.class},
            maxAttempts = 3,
            backoff     = @Backoff(delay = 2000, multiplier = 2)
    )
    public void processJob(String jobIdStr) {
        UUID jobId = UUID.fromString(jobIdStr);

        // Bước 1: Tìm job trong DB
        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new RuntimeException("Job not found: " + jobIdStr));

        // Bước 2: Đánh dấu đang xử lý
        job.setStatus(JobStatus.PROCESSING);
        jobRepository.save(job);
        log.info("[EmailWorker] Processing | jobId={}", jobId);

        try {
            // Bước 3: Parse payload JSON → EmailPayload object
            EmailPayload payload = objectMapper.readValue(
                    job.getPayload(), EmailPayload.class);
            // ── AI LAYER: Chỉ chạy nếu có context và chưa có body ──
            if (payload.getBody() == null && payload.getContext() != null) {

                String aiBody = emailComposerAI.generateBody(
                        payload.getContext(),
                        payload.getRecipientName()
                );

                // AI thành công → dùng body AI generate
                // AI fail (null) → dùng context làm body thô (fallback)
                // Hệ thống KHÔNG bao giờ fail vì AI
                if (aiBody != null) {
                    payload.setBody(aiBody);
                    log.info("[EmailProcessor] Using AI-generated body | jobId={}", jobId);
                } else {
                    payload.setBody(payload.getContext());
                    log.warn("[EmailProcessor] AI failed, using context as body | jobId={}", jobId);
                }
            }
            // Suggest subject tốt hơn nếu có context
            if (payload.getContext() != null && payload.getSubject() != null) {
                String betterSubject = emailComposerAI.suggestSubject(
                        payload.getSubject(), payload.getContext());
                if (betterSubject != null) {
                    payload.setSubject(betterSubject.trim());
                }
            }
            // Bước 4: Gửi email
            emailService.send(payload);

            // Bước 5: Cập nhật DONE
            job.setStatus(JobStatus.DONE);
            jobRepository.save(job);
            log.info("[EmailWorker] Done | jobId={} | to={}",
                    jobId, payload.getTo());

            // Bước 6: Gọi webhook notify client
            // Chạy trên webhookExecutor — không block worker thread này
            webhookService.deliver(job);

        } catch (Exception e) {
            log.error("[EmailWorker] Error | jobId={} | msg={}",
                    jobId, e.getMessage());
            // Ném lại để @Retryable bắt và retry
            throw new RuntimeException("Email job failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────
    // @Recover chạy sau khi hết tất cả retry
    //
    // Lưu ý: signature phải có Exception là tham số đầu tiên
    // + đúng các tham số của method gốc
    // ─────────────────────────────────────────────────────────
    @Recover
    public void recover(RuntimeException e, String jobIdStr) {
        log.error("[EmailWorker] All retries failed | jobId={} | error={}",
                jobIdStr, e.getMessage());

        try {
            UUID jobId = UUID.fromString(jobIdStr);
            Job job = jobRepository.findById(jobId).orElse(null);

            if (job != null) {
                // Đánh dấu FAILED
                job.setStatus(JobStatus.FAILED);
                job.setRetryCount(job.getRetryCount() + 1);
                jobRepository.save(job);

                // Notify client biết job thất bại
                webhookService.deliver(job);
            }

        } catch (Exception ex) {
            log.error("[EmailWorker] Recover error | jobId={}", jobIdStr, ex);
        } finally {
            // Luôn đẩy vào Dead Letter Queue dù có lỗi hay không
            redisTemplate.opsForList().leftPush(DEAD_LETTER_QUEUE, jobIdStr);
            log.warn("[EmailWorker] Pushed to DLQ | jobId={}", jobIdStr);
        }
    }
}