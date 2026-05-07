package com.trongtin.asyncprocessingsys.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trongtin.asyncprocessingsys.ai.JobClassifierAI;
import com.trongtin.asyncprocessingsys.dto.request.CreateJobRequest;
import com.trongtin.asyncprocessingsys.dto.response.JobResponse;
import com.trongtin.asyncprocessingsys.model.Job;
import com.trongtin.asyncprocessingsys.model.enums.JobPriority;
import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.model.enums.JobType;
import com.trongtin.asyncprocessingsys.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

// service/JobService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String EMAIL_QUEUE = "queue:email";
    private static final String PDF_QUEUE = "queue:pdf";

    private final JobClassifierAI jobClassifierAI;

    private static final String EMAIL_QUEUE_HIGH = "queue:email:high";
    private static final String EMAIL_QUEUE_MEDIUM = "queue:email:medium";
    private static final String EMAIL_QUEUE_LOW = "queue:email:low";
    private static final String PDF_QUEUE_HIGH = "queue:pdf:high";
    private static final String PDF_QUEUE_MEDIUM = "queue:pdf:medium";
    private static final String PDF_QUEUE_LOW = "queue:pdf:low";


    public JobResponse createJob(CreateJobRequest request) {

        JobPriority priority = jobClassifierAI.classify(
                request.getPayload(),
                request.getType()
        );

        Job job = Job.builder()
                .type(request.getType())
                .status(JobStatus.PENDING)
                .payload(request.getPayload())
                .callbackUrl(request.getCallbackUrl())
                .priority(priority)
                .build();

        job = jobRepository.save(job);
        log.info("[JobService] Created | jobId={} | type={} | priority={}",
                job.getId(), job.getType(), priority);
        String streamKey = resolveStream(request.getType(), priority);

        RecordId recordId = redisTemplate.opsForStream().add(
                MapRecord.create(streamKey, Map.of(
                        "jobId", job.getId().toString(),
                        "type", request.getType().name()
                ))
        );
        log.info("[JobService] Created | recordId={} ",
                recordId);
        // ────────────────────────────────
        return toResponse(job);
    }

    public JobResponse getJobStatus(UUID jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        return toResponse(job);
    }


    private String resolveStream(JobType type, JobPriority priority) {
        String base = switch (type) {
            case EMAIL -> "stream:email";
            case EXPORT_PDF -> "stream:pdf";
        };
        String suffix = switch (priority) {
            case HIGH -> ":high";
            case MEDIUM -> ":medium";
            case LOW -> ":low";
        };
        log.info("[JopType After Resolve]  | type={} | priority={} | Result={}", base, priority, base + suffix);

        return base + suffix;
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