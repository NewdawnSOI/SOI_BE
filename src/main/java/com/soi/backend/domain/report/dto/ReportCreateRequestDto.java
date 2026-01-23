package com.soi.backend.domain.report.dto;

import com.soi.backend.domain.report.entity.ReportStatus;
import com.soi.backend.domain.report.entity.ReportType;
import com.soi.backend.domain.report.entity.ReportTargetType;
import lombok.Getter;

@Getter
public class ReportCreateRequestDto {
    private Long reporterUserId;
    private Long targetId;
    private ReportTargetType reportTargetType;
    private ReportType reportType;
    private String reportDetail;
}
