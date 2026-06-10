package com.trongtin.asyncprocessingsys.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketDTO {
    private Long id;

    private String name;

    private String description;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String image;
    private int status;
    private java.math.BigDecimal priceOriginal;
    private java.math.BigDecimal priceFlash;
    private int stockAvailable;
    private int stockInitial;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
