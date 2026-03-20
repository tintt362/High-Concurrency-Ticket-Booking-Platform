package com.trongtin.asyncprocessingsys.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebhookPayload {
    private String jobId;
    private String jobType;
    private String status;    // DONE hoặc FAILED
    private String result;    // null với email job
    private String timestamp;
}