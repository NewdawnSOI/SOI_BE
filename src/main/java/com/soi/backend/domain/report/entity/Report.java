package com.soi.backend.domain.report.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor

@Table(name = "report", schema = "soi")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_user_id", nullable = false)
    private Long reporterUserId;

    @Column(name = "target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportTargetType reportTargetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "report_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Column(name = "report_detail")
    private String reportDetail;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Column(name = "admin_memo")
    private String adminMemo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public Report(
            Long reporterUserId,
            ReportTargetType reportTargetType,
            Long targetId,
            ReportType reportType,
            String reportDetail,
            ReportStatus reportStatus,
            String adminMemo) {
        this.reporterUserId = reporterUserId;
        this.reportTargetType = reportTargetType;
        this.targetId = targetId;
        this.reportType = reportType;
        this.reportDetail = reportDetail;
        this.status = reportStatus;
        this.adminMemo = adminMemo;
        this.createdAt = LocalDateTime.now();
    }

    public void setUpdate(ReportStatus reportStatus, String adminMemo) {
        this.status = reportStatus;
        this.adminMemo = adminMemo;
        this.processedAt = LocalDateTime.now();
    }
}
