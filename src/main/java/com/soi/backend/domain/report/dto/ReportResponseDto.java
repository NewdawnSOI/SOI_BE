package com.soi.backend.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soi.backend.domain.report.entity.ReportStatus;
import com.soi.backend.domain.report.entity.ReportType;
import com.soi.backend.domain.report.entity.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter

public class ReportResponseDto {
    private Long id;
    private Long reporterUserId;
    private Long targetId;
    private ReportTargetType reportTargetType;
    private ReportType reportType;
    private ReportStatus reportStatus;
    private String reportDetail;
    private String adminMemo;
    private LocalDateTime createTime;

    private LocalDateTime processTime;
}
