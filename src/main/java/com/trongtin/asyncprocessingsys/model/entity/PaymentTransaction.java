package com.trongtin.asyncprocessingsys.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentTransaction {
    private Long id;
    private String paymentId;
    private String orderNumber;
    private Integer userId;
    private BigDecimal amount;
    private String paymentMethod;
    private Integer paymentStatus; // 0:INIT, 1:IN_PROGRESS, 2:SUCCESS, 3:FAILED
    private String gatewayTransactionId;
    private String paymentUrl;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
