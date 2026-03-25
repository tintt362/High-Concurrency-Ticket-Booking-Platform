package com.trongtin.asyncprocessingsys.model;

import com.trongtin.asyncprocessingsys.model.enums.JobPriority;
import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.model.enums.JobType;
import com.trongtin.asyncprocessingsys.model.enums.WebhookStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

// model/Job.java
@Entity
@Table(name = "jobs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    // Lưu JSON payload: { "to": "...", "subject": "..." }
    @Column(columnDefinition = "TEXT")
    private String payload;

    // URL download nếu là PDF, null nếu là email
    private String result;

    // Client đăng ký để nhận callback
    private String callbackUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WebhookStatus webhookStatus = WebhookStatus.PENDING;

    @Builder.Default
    private int retryCount = 0;

    // Thêm vào sau field result — KHÔNG xóa gì cũ
    @Column(columnDefinition = "TEXT")
    private String aiSummary;
// AI tóm tắt nội dung PDF (do ReportAnalyzerAI tạo)

    // Thêm vào Job.java cùng chỗ với aiSummary
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private JobPriority priority = JobPriority.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String aiInsights;
// AI đề xuất hành động (do ReportAnalyzerAI tạo)

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}