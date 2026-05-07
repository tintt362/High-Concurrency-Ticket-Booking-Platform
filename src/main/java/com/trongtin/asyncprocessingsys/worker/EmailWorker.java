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

    @Scheduled(fixedDelay = 10000)
    public void pollQueue() {

        String jobIdStr = redisTemplate.opsForList()
                .rightPop("queue:email:high");

        if (jobIdStr == null) {
            jobIdStr = redisTemplate.opsForList()
                    .rightPop("queue:email:medium");
        }

        if (jobIdStr == null) {
            jobIdStr = redisTemplate.opsForList()
                    .rightPop("queue:email:low");
        }

        if (jobIdStr == null) return;

        log.info("[EmailWorker] Dequeued | jobId={}", jobIdStr);
        processJob(jobIdStr);
    }

    @Retryable(
            retryFor    = {Exception.class},
            maxAttempts = 3,
            backoff     = @Backoff(delay = 2000, multiplier = 2)
    )
    public void processJob(String jobIdStr) {
        UUID jobId = UUID.fromString(jobIdStr);

        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new RuntimeException("Job not found: " + jobIdStr));

        job.setStatus(JobStatus.PROCESSING);
        jobRepository.save(job);
        log.info("[EmailWorker] Processing | jobId={}", jobId);

        try {
            EmailPayload payload = objectMapper.readValue(
                    job.getPayload(), EmailPayload.class);
            if (payload.getBody() == null && payload.getContext() != null) {

                String aiBody = emailComposerAI.generateBody(
                        payload.getContext(),
                        payload.getRecipientName()
                );


                if (aiBody != null) {
                    payload.setBody(aiBody);
                    log.info("[EmailProcessor] Using AI-generated body | jobId={}", jobId);
                } else {
                    payload.setBody(payload.getContext());
                    log.warn("[EmailProcessor] AI failed, using context as body | jobId={}", jobId);
                }
            }
            if (payload.getContext() != null && payload.getSubject() != null) {
                String betterSubject = emailComposerAI.suggestSubject(
                        payload.getSubject(), payload.getContext());
                if (betterSubject != null) {
                    payload.setSubject(betterSubject.trim());
                }
            }
            emailService.send(payload);

            job.setStatus(JobStatus.DONE);
            jobRepository.save(job);
            log.info("[EmailWorker] Done | jobId={} | to={}",
                    jobId, payload.getTo());


            webhookService.deliver(job);

        } catch (Exception e) {
            log.error("[EmailWorker] Error | jobId={} | msg={}",
                    jobId, e.getMessage());
            throw new RuntimeException("Email job failed: " + e.getMessage(), e);
        }
    }

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
            redisTemplate.opsForList().leftPush(DEAD_LETTER_QUEUE, jobIdStr);
            log.warn("[EmailWorker] Pushed to DLQ | jobId={}", jobIdStr);
        }
    }
}