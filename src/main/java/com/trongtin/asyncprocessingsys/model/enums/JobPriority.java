package com.trongtin.asyncprocessingsys.model.enums;

public enum JobPriority {
    HIGH,    // Khẩn cấp — xử lý ngay lập tức
    MEDIUM,  // Bình thường — mặc định
    LOW      // Không gấp — defer về giờ thấp điểm
}