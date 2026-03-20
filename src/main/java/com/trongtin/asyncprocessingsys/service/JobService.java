package com.trongtin.asyncprocessingsys.service;

import com.trongtin.asyncprocessingsys.dto.request.CreateJobRequest;
import com.trongtin.asyncprocessingsys.dto.response.JobResponse;
import com.trongtin.asyncprocessingsys.model.Job;
import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.model.enums.JobType;
import com.trongtin.asyncprocessingsys.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

// service/JobService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // Tên các queue trong Redis
    private static final String EMAIL_QUEUE    = "queue:email";
    private static final String PDF_QUEUE      = "queue:pdf";

    public JobResponse createJob(CreateJobRequest request) {
        // 1. Tạo và lưu job vào DB với status PENDING
        Job job = Job.builder()
                .type(request.getType())
                .status(JobStatus.PENDING)
                .payload(request.getPayload())
                .callbackUrl(request.getCallbackUrl())
                .build();

        job = jobRepository.save(job);
        log.info("[JobService] Created job | jobId={} | type={}", job.getId(), job.getType());

        // 2. Đẩy jobId vào Redis queue tương ứng
        String queueName = resolveQueue(request.getType());
        redisTemplate.opsForList().leftPush(queueName, job.getId().toString());
        log.info("[JobService] Pushed to queue | queue={} | jobId={}", queueName, job.getId());

        return toResponse(job);
    }

    public JobResponse getJobStatus(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        return toResponse(job);
    }

    private String resolveQueue(JobType type) {
        return switch (type) {
            case EMAIL      -> EMAIL_QUEUE;
            case EXPORT_PDF -> PDF_QUEUE;
        };
    }

    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .jobId(job.getId())
                .type(job.getType())
                .status(job.getStatus())
                .result(job.getResult())
                .webhookStatus(job.getWebhookStatus())
                .createdAt(job.getCreatedAt())
                .build();
    }
}