package com.soi.backend.domain.report.dto;

import com.soi.backend.common.dto.SortOptionDto;
import com.soi.backend.domain.report.entity.ReportStatus;
import com.soi.backend.domain.report.entity.ReportType;
import com.soi.backend.domain.report.entity.ReportTargetType;
import lombok.Getter;

@Getter

public class ReportSearchRequestDto {
    private ReportType reportType;
    private ReportStatus reportStatus;
    private ReportTargetType reportTargetType;

    private SortOptionDto sortOptionDto;

    private int page;
}
