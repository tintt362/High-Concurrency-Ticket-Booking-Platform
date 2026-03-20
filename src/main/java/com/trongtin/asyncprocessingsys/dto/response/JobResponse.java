package com.trongtin.asyncprocessingsys.dto.response;

import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.model.enums.JobType;
import com.trongtin.asyncprocessingsys.model.enums.WebhookStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Setter
@Getter
public class JobResponse {
    private UUID jobId;
    private JobType type;
    private JobStatus status;
    private String result;
    private WebhookStatus webhookStatus;
    private LocalDateTime createdAt;
}