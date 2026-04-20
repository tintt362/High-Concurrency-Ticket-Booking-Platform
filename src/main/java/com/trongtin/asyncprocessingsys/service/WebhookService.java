package com.trongtin.asyncprocessingsys.service;



import com.trongtin.asyncprocessingsys.dto.response.WebhookPayload;
import com.trongtin.asyncprocessingsys.model.Job;
import com.trongtin.asyncprocessingsys.model.enums.WebhookStatus;
import com.trongtin.asyncprocessingsys.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookService {

    private final RestTemplate restTemplate;
    private final JobRepository jobRepository;

    // Exponential backoff: 10s → 30s → 90s
    private static final long[] RETRY_DELAYS = {10, 30, 90};
    private static final int MAX_RETRY = 3;

    // Chạy trên webhookExecutor — không block worker thread
    @Async("webhookExecutor")
    public void deliver(Job job) {
        if (job.getCallbackUrl() == null || job.getCallbackUrl().isBlank()) {
            log.debug("[WebhookService] No callbackUrl | jobId={}", job.getId());
            return;
        }
        log.info("[WebhookService] Start delivery | jobId={} | url={}",
                job.getId(), job.getCallbackUrl());
        attemptDelivery(job, 0);
    }

    private void attemptDelivery(Job job, int attempt) {
        WebhookPayload payload = buildPayload(job);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    job.getCallbackUrl(), payload, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                markDelivered(job);
                log.info("[WebhookService] Delivered OK | jobId={} | attempt={}",
                        job.getId(), attempt + 1);
            } else {
                handleFailure(job, attempt,
                        "HTTP " + response.getStatusCode().value());
            }

        } catch (Exception e) {
            handleFailure(job, attempt, e.getMessage());
        }
    }

    private void handleFailure(Job job, int attempt, String reason) {
        log.warn("[WebhookService] Attempt {} failed | jobId={} | reason={}",
                attempt + 1, job.getId(), reason);

        if (attempt < MAX_RETRY - 1) {
            long delaySec = RETRY_DELAYS[attempt];
            log.info("[WebhookService] Retry in {}s | jobId={}", delaySec, job.getId());

            // Schedule retry — không block thread hiện tại
            CompletableFuture
                    .delayedExecutor(delaySec, TimeUnit.SECONDS)
                    .execute(() -> attemptDelivery(job, attempt + 1));

            // Ghi lại số lần retry
            job.setRetryCount(attempt + 1);
            jobRepository.save(job);

        } else {
            // Hết retry → FAILED
            markFailed(job);
            log.error("[WebhookService] Max retry reached | jobId={} | url={}",
                    job.getId(), job.getCallbackUrl());
        }
    }

    private WebhookPayload buildPayload(Job job) {
        return WebhookPayload.builder()
                .jobId(job.getId().toString())
                .jobType(job.getType().name())
                .status(job.getStatus().name())
                .result(job.getResult())
                .timestamp(Instant.now().toString())
                .build();
    }

    private void markDelivered(Job job) {
        job.setWebhookStatus(WebhookStatus.DELIVERED);
        jobRepository.save(job);
    }

    private void markFailed(Job job) {
        job.setWebhookStatus(WebhookStatus.FAILED);
        jobRepository.save(job);
    }
}