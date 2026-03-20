package com.trongtin.asyncprocessingsys.worker;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.trongtin.asyncprocessingsys.dto.request.EmailPayload;
import com.trongtin.asyncprocessingsys.model.Job;
import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailWorker {

    private final RedisTemplate<String, String> redisTemplate;
    private final JobRepository jobRepository;
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
  //  private final WebhookService webhookService;   // 👈 thêm

    private static final String EMAIL_QUEUE = "queue:email";

    // ─────────────────────────────────────────────
    // Poll queue:email mỗi 2 giây
    // fixedDelay: chờ 2s SAU KHI lần trước xong
    // tránh overlap nếu xử lý lâu hơn 2s
    // ─────────────────────────────────────────────
    @Scheduled(fixedDelay = 2000)
    public void pollQueue() {
        // RPOP lấy jobId từ cuối queue — trả null nếu queue rỗng
        String jobIdStr = redisTemplate.opsForList().rightPop(EMAIL_QUEUE);

        if (jobIdStr == null) {
            // Queue rỗng — không log để tránh spam console
            return;
        }

        log.info("[EmailWorker] Picked job from queue | jobId={}", jobIdStr);
        processJob(jobIdStr);
    }

    // ─────────────────────────────────────────────
    // Xử lý từng job
    // @Retryable: tự retry tối đa 3 lần nếu lỗi
    // delay tăng dần: 2s → 4s → 8s (multiplier=2)
    // ─────────────────────────────────────────────
    @Retryable(
            retryFor  = { Exception.class },
            maxAttempts = 3,
            backoff  = @Backoff(delay = 2000, multiplier = 2)
    )
    public void processJob(String jobIdStr) {
        UUID jobId = UUID.fromString(jobIdStr);

        // Bước 1: Tìm job trong DB
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("[EmailWorker] Job not found | jobId={}", jobIdStr);
                    return new RuntimeException("Job not found: " + jobIdStr);
                });

        // Bước 2: Cập nhật status PROCESSING
        job.setStatus(JobStatus.PROCESSING);
        jobRepository.save(job);
        log.info("[EmailWorker] Processing | jobId={}", jobId);

        try {
            // Bước 3: Parse payload JSON
            EmailPayload payload = objectMapper.readValue(
                    job.getPayload(), EmailPayload.class);

            // Bước 4: Gửi email
            sendEmail(payload);

            // Bước 5: Cập nhật DONE
            job.setStatus(JobStatus.DONE);
            jobRepository.save(job);
            log.info("[EmailWorker] Done | jobId={} | to={}", jobId, payload.getTo());

            // Bước 6: Notify client qua webhook
          //  webhookService.deliver(job);   // 👈 thêm

        } catch (Exception e) {
            log.error("[EmailWorker] Failed | jobId={} | error={}", jobId, e.getMessage());
            // Ném lại để @Retryable bắt và retry
            throw new RuntimeException("Email processing failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    // Gửi email qua JavaMailSender
    // ─────────────────────────────────────────────
    private void sendEmail(EmailPayload payload) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("noreply@asyncjob.com");
        helper.setTo(payload.getTo());
        helper.setSubject(payload.getSubject());
        // true = HTML content
        helper.setText(buildHtmlBody(payload.getBody()), true);

        mailSender.send(message);
        log.debug("[EmailWorker] Email sent | to={}", payload.getTo());
    }

    // ─────────────────────────────────────────────
    // Template HTML đơn giản cho email
    // ─────────────────────────────────────────────
    private String buildHtmlBody(String content) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="color: #333;">Thông báo từ Async Job System</h2>
                <p>%s</p>
                <hr/>
                <small style="color: #999;">Email được gửi tự động, vui lòng không reply.</small>
            </body>
            </html>
            """.formatted(content);
    }

    // ─────────────────────────────────────────────
    // @Recover: chạy khi đã hết tất cả retry
    // Cập nhật status FAILED và đẩy vào Dead Letter Queue
    // ─────────────────────────────────────────────
    @Recover
    public void recover(RuntimeException e, String jobIdStr) {
        log.error("[EmailWorker] Max retry reached | jobId={} | error={}",
                jobIdStr, e.getMessage());

        try {
            UUID jobId = UUID.fromString(jobIdStr);
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job != null) {
                job.setStatus(JobStatus.FAILED);
                job.setRetryCount(job.getRetryCount() + 1);
                jobRepository.save(job);
            }

            // Đẩy vào Dead Letter Queue
            redisTemplate.opsForList().leftPush("queue:dead-letter", jobIdStr);
            log.warn("[EmailWorker] Pushed to DLQ | jobId={}", jobIdStr);

        } catch (Exception ex) {
            log.error("[EmailWorker] Recover failed | jobId={}", jobIdStr, ex);
        }
    }
}