package com.soi.backend.domain.report.dto;

import com.soi.backend.domain.report.entity.ReportStatus;
import lombok.Getter;

@Getter

public class ReportUpdateReqDto {
    private Long id;
    private ReportStatus reportStatus;
    private String adminMemo;
}
