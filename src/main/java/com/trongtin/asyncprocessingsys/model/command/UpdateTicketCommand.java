package com.trongtin.asyncprocessingsys.model.command;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateTicketCommand {
    private String title;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private BigDecimal price;
    private Boolean active;
}
