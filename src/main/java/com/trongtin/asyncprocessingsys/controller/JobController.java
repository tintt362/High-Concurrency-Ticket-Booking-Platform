package com.trongtin.asyncprocessingsys.controller;

import com.trongtin.asyncprocessingsys.dto.request.CreateJobRequest;
import com.trongtin.asyncprocessingsys.dto.response.ApiResponse;
import com.trongtin.asyncprocessingsys.dto.response.JobResponse;
import com.trongtin.asyncprocessingsys.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// controller/JobController.java
@RestController
@RequestMapping("/api/v1/jobs")
@Slf4j
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody CreateJobRequest request) {

        log.info("[JobController] Create job request | type={}", request.getType());
        JobResponse response = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<ApiResponse<JobResponse>> getStatus(
            @PathVariable UUID jobId) {

        JobResponse response = jobService.getJobStatus(jobId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}