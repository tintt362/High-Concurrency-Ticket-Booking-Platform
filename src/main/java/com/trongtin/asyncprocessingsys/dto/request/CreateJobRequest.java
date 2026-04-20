package com.trongtin.asyncprocessingsys.dto.request;

import com.trongtin.asyncprocessingsys.model.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

// dto/request/CreateJobRequest.java
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobRequest {

    @NotNull(message = "Job type is required")
    private JobType type;

    @NotBlank(message = "Payload is required")
    private String payload;        // JSON string

    private String callbackUrl;    // Optional
}