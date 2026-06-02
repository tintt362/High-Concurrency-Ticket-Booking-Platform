package com.xxxx.ddd.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBookingRequest {
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Quantity cannot exceed 10")
    private int quantity;
}