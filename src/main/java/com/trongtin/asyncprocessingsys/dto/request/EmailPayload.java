package com.trongtin.asyncprocessingsys.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailPayload {
    private String to;        // Địa chỉ nhận
    private String subject;   // Tiêu đề
    private String body;      // Nội dung
}
