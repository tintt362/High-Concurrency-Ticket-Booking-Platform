package com.trongtin.asyncprocessingsys.model.enums;

public enum WebhookStatus {
    PENDING,    // Chưa gửi
    DELIVERED,  // Gửi thành công
    FAILED      // Thất bại sau max retry
}
