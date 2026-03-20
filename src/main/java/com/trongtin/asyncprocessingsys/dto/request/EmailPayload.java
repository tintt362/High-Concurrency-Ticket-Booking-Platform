package com.trongtin.asyncprocessingsys.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailPayload {
    private String to;        // Địa chỉ nhận
    private String subject;
    private String recipientName; // Thêm field này// Tiêu đề
    private String body;      // Nội dung
}
