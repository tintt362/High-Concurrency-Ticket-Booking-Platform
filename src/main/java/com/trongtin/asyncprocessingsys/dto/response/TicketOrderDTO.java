package com.trongtin.asyncprocessingsys.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketOrderDTO {
    private Integer id;
    private Integer userId;

    // -- Bổ sung 3 trường mới tương ứng Database --
    private Integer ticketId;
    private Integer quantity;
    private Integer orderStatus;
    // --------------------------------------------
    private String orderNumber;
    private BigDecimal totalAmount;
    private String terminalId;
    private LocalDateTime orderDate;
    private String orderNotes;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}