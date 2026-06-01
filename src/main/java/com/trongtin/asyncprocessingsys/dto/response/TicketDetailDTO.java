package com.trongtin.asyncprocessingsys.dto.response;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketDetailDTO {

    private Long id;
    private String name;
    private int stockInitial;
    private int stockAvailable;
    private boolean isStockPrepared;
    private Long priceOriginal;
    private Long priceFlash;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private int status;
    private Long activityId;
    // add version
    private Long version;
}

