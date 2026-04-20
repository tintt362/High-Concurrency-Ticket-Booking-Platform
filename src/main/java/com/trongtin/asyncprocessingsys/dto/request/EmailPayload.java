package com.trongtin.asyncprocessingsys.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailPayload {
    private String to;
    private String subject;
    private String body;        // Có sẵn → dùng luôn, không cần AI
    private String context;     // Mô tả ngắn → AI generate body
    private String recipientName;
}
