package com.xxxx.ddd.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class CreateTicketDetailRequest {

    @NotBlank(message = "Ticket detail name cannot be empty")
    private String name;

    private String description;

    @NotNull
    @Min(1)
    private Integer stockInitial;

    @NotNull
    @Min(0)
    private Integer stockAvailable;

    @NotNull
    @Min(1)
    private Long priceOriginal;

    private Long priceFlash;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date saleStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date saleEndTime;

    private Boolean stockPrepared;
}
