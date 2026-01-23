package com.soi.backend.domain.report.repository;

import com.soi.backend.domain.report.dto.ReportSearchRequestDto;
import com.soi.backend.domain.report.entity.Report;

import java.util.List;

public interface ReportQueryRepository {
    List<Report> findAllByOption(ReportSearchRequestDto searchRequestDto);
}
