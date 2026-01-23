package com.soi.backend.domain.report.controller;

import com.soi.backend.domain.report.dto.ReportCreateRequestDto;
import com.soi.backend.domain.report.dto.ReportResponseDto;
import com.soi.backend.domain.report.dto.ReportSearchRequestDto;
import com.soi.backend.domain.report.dto.ReportUpdateReqDto;
import com.soi.backend.domain.report.service.ReportService;
import com.soi.backend.global.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")

public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "신고 추가", description = "신고 내용을 추가합니다.")
    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<Boolean>> create(@RequestBody ReportCreateRequestDto reportCreateRequestDto) {
        Boolean result = reportService.createReport(reportCreateRequestDto);
        return ResponseEntity.ok(ApiResponseDto.success(result,"신고 추가 완료"));
    }

    @Operation(summary = "신고 내용 조회", description = "신고 내용을 조회합니다.")
    @PostMapping("/find")
    public ResponseEntity<ApiResponseDto<List<ReportResponseDto>>> find(@RequestBody ReportSearchRequestDto reportSearchRequestDto) {
        System.out.println("reportType = [" + reportSearchRequestDto.getReportType() + "]");
        List<ReportResponseDto> reports = reportService.getReports(reportSearchRequestDto);
        return ResponseEntity.ok(ApiResponseDto.success(reports,"신고 조회 완료"));
    }

    @Operation(summary = "신고 상태 업데이트", description = "신고 상태를 업데이트 및 관리자 커멘트를 추가합니다.")
    @PatchMapping("/update")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> update(@RequestBody ReportUpdateReqDto reportUpdateReqDto) {
        ReportResponseDto result = reportService.updateReport(reportUpdateReqDto);
        return ResponseEntity.ok(ApiResponseDto.success(result,"신고 업데이트 완료"));
    }

    @Operation(summary = "신고 삭제", description = "id값으로 신고 삭제합니다.")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseDto<Boolean>> delete(@RequestParam Long id) {
        Boolean result = reportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponseDto.success(result,"신고 삭제 완료"));
    }

}
