package com.xxxx.ddd.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class CreateTicketRequest {
    @NotBlank(message = "Ticket name cannot be empty")
    @Size(max = 255, message = "Ticket name cannot exceed 255 characters")
    private String name;

    @Size(min = 0, max = 500, message = "Description must be between 0 and 500 characters")
    private String description;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
