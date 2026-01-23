package com.soi.backend.domain.report.service;

import com.soi.backend.domain.report.dto.ReportCreateRequestDto;
import com.soi.backend.domain.report.dto.ReportResponseDto;
import com.soi.backend.domain.report.dto.ReportSearchRequestDto;
import com.soi.backend.domain.report.dto.ReportUpdateReqDto;
import com.soi.backend.domain.report.entity.Report;
import com.soi.backend.domain.report.entity.ReportStatus;
import com.soi.backend.domain.report.repository.ReportQueryRepositoryImpl;
import com.soi.backend.domain.report.repository.ReportRepository;
import com.soi.backend.global.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class ReportService {
    private final ReportRepository reportRepository;

    // 신고 생성함수
    @Transactional
    public Boolean createReport(ReportCreateRequestDto dto) {
        Report report = new Report(
                dto.getReporterUserId(),
                dto.getReportTargetType(),
                dto.getTargetId(),
                dto.getReportType(),
                dto.getReportDetail().isBlank() ? "" : dto.getReportDetail(),
                ReportStatus.PADDING,
                ""
        );

        reportRepository.save(report);
        return true;
    }

    // 필터링 옵션에 따라서 신고목록 뿌리는 함수
    public List<ReportResponseDto> getReports(ReportSearchRequestDto searchRequestDto) {
        List<Report> reports = reportRepository.findAllByOption(searchRequestDto);

        List<ReportResponseDto> reportResponseDtos = new ArrayList<>();
        for (Report report : reports) {
            ReportResponseDto reportResponseDto = toDto(report);
            reportResponseDtos.add(reportResponseDto);
        }

        return reportResponseDtos;
    }

    // 신고 상태 업데이트 함수
    @Transactional
    public ReportResponseDto updateReport(ReportUpdateReqDto updateRequestDto) {
        Report report = reportRepository.findById(updateRequestDto.getId())
                .orElseThrow(() -> new CustomException("리포트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        report.setUpdate(
                updateRequestDto.getReportStatus(),
                updateRequestDto.getAdminMemo()
        );

        reportRepository.save(report);

        return toDto(report);
    }

    // 신고 삭제
    @Transactional
    public Boolean deleteReport(Long id) {
        reportRepository.deleteById(id);
        return true;
    }

    public ReportResponseDto toDto(Report report) {
        return new ReportResponseDto(
                report.getId(),
                report.getReporterUserId(),
                report.getTargetId(),
                report.getReportTargetType(),
                report.getReportType(),
                report.getStatus(),
                report.getReportDetail(),
                report.getAdminMemo(),
                report.getCreatedAt(),
                report.getProcessedAt()
        );
    }
}
