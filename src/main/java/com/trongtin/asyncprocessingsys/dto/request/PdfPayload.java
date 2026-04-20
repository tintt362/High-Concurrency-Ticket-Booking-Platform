package com.trongtin.asyncprocessingsys.dto.request;


import lombok.Data;

@Data
public class PdfPayload {

    // Loại báo cáo — dùng để chọn template
    // Ví dụ: "monthly_revenue", "invoice", "employee_list"
    private String reportType;

    // Tiêu đề hiển thị trên PDF
    private String title;

    // Tháng/kỳ báo cáo — "2024-11"
    private String period;

    private String content;
    // Tên người yêu cầu — hiển thị trên PDF
    private String requestedBy;

    // Nội dung tóm tắt — tuỳ loại báo cáo
    private String summary;
}