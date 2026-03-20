package com.trongtin.asyncprocessingsys.model.enums;

public enum JobStatus {

    PENDING,      // Vừa tạo, chờ worker lấy
    PROCESSING,   // Worker đang xử lý
    DONE,         // Xong thành công
    FAILED        // Thất bại sau retry
}
