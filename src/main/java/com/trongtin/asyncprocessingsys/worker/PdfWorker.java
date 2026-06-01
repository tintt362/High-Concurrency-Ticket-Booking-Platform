package com.trongtin.asyncprocessingsys.worker;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.trongtin.asyncprocessingsys.ai.ReportAnalyzerAI;
import com.trongtin.asyncprocessingsys.dto.request.PdfPayload;
import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.repository.JobRepository;
import com.trongtin.asyncprocessingsys.service.PdfService;
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
public class PdfWorker {

    private final RedisTemplate<String, String> redisTemplate;
    private final JobRepository jobRepository;
    private final PdfService pdfService;
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    private final ReportAnalyzerAI reportAnalyzerAI;

    private static final String PDF_QUEUE        = "queue:pdf";
    private static final String DEAD_LETTER_QUEUE = "queue:dead-letter";

    // Poll queue:pdf mỗi 2 giây
    @Scheduled(fixedDelay = 2000)
    public void pollQueue() {
        String jobIdStr = redisTemplate.opsForList().rightPop(PDF_QUEUE);
        if (jobIdStr == null) return;

        log.info("[PdfWorker] Dequeued | jobId={}", jobIdStr);
        processJob(jobIdStr);
    }

    @Retryable(
            retryFor    = {Exception.class},
            maxAttempts = 3,
            backoff     = @Backoff(delay = 2000, multiplier = 2)
    )
    public void processJob(String jobIdStr) {
        UUID jobId = UUID.fromString(jobIdStr);

        // Bước 1: Tìm job
        Job job = jobRepository.findById(jobId).orElseThrow(
                () -> new RuntimeException("Job not found: " + jobIdStr));

        // Bước 2: Đánh dấu PROCESSING
        job.setStatus(JobStatus.PROCESSING);
        jobRepository.save(job);
        log.info("[PdfWorker] Processing | jobId={}", jobId);

        try {
            // Bước 3: Parse payload
            PdfPayload payload = objectMapper.readValue(
                    job.getPayload(), PdfPayload.class);

            String downloadUrl = pdfService.generate(jobId, payload);
            String summary  = reportAnalyzerAI.analyze(
                    payload.getContent(), payload.getReportType());
            String insights = reportAnalyzerAI.generateInsights(
                    payload.getContent(), payload.getReportType());


            job.setStatus(JobStatus.DONE);
            job.setResult(downloadUrl);
            job.setAiSummary(summary);    // ← lưu AI summary
            job.setAiInsights(insights);  // ← lưu AI insights
            jobRepository.save(job);
            log.info("[PdfWorker] Done | jobId={} | url={}", jobId, downloadUrl);

            webhookService.deliver(job);

        } catch (Exception e) {
            log.error("[PdfWorker] Error | jobId={} | msg={}", jobId, e.getMessage());
            throw new RuntimeException("PDF job failed: " + e.getMessage(), e);
        }
    }

    @Recover
    public void recover(RuntimeException e, String jobIdStr) {
        log.error("[PdfWorker] All retries failed | jobId={} | error={}",
                jobIdStr, e.getMessage());

        try {
            UUID jobId = UUID.fromString(jobIdStr);
            Job job = jobRepository.findById(jobId).orElse(null);

            if (job != null) {
                job.setStatus(JobStatus.FAILED);
                job.setRetryCount(job.getRetryCount() + 1);
                jobRepository.save(job);
                webhookService.deliver(job);
            }

        } catch (Exception ex) {
            log.error("[PdfWorker] Recover error | jobId={}", jobIdStr, ex);
        } finally {
            redisTemplate.opsForList().leftPush(DEAD_LETTER_QUEUE, jobIdStr);
            log.warn("[PdfWorker] Pushed to DLQ | jobId={}", jobIdStr);
        }
    }
}