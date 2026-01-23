package com.soi.backend.domain.report.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soi.backend.common.entity.SortDirection;
import com.soi.backend.common.entity.SortField;
import com.soi.backend.domain.report.dto.ReportSearchRequestDto;
import com.soi.backend.domain.report.entity.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ReportQueryRepositoryImpl implements ReportQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Report> findAllByOption(ReportSearchRequestDto dto) {

        QReport r = QReport.report;

        // 정렬 옵션 처리: null이면 기본 정렬
        OrderSpecifier<?> order = r.createdAt.desc(); // 기본값
        if (dto.getSortOptionDto() != null && dto.getSortOptionDto().getSortField() != null) {
            order = getOrderSpecifier(dto);
        }

        // 페이징 기본값 설정
        int page = dto.getPage(); // 필수 값
        int pageSize = 20; // 원하는 페이지 사이즈

        return queryFactory
                .selectFrom(r)
                .where(
                        reportTargetTypeEq(dto.getReportTargetType()),
                        reportTypeEq(dto.getReportType()),
                        reportStatusEq(dto.getReportStatus())
                )
                .orderBy(order)
                .offset((long) page * pageSize)
                .limit(pageSize)
                .fetch();
    }

    /* =======================
       조건 메서드
       ======================= */

    private BooleanExpression reportTargetTypeEq(ReportTargetType type) {
        return type != null ? QReport.report.reportTargetType.eq(type) : null;
    }

    private BooleanExpression reportTypeEq(ReportType type) {
        return type != null ? QReport.report.reportType.eq(type) : null;
    }

    private BooleanExpression reportStatusEq(ReportStatus status) {
        return status != null ? QReport.report.status.eq(status) : null;
    }

    /* =======================
       정렬 메서드 (핵심)
       ======================= */

    private OrderSpecifier<?> getOrderSpecifier(ReportSearchRequestDto dto) {

        SortField field = dto.getSortOptionDto().getSortField();
        SortDirection direction = dto.getSortOptionDto().getDirection();

        return switch (field) {
            case CREATED_AT ->
                    direction == SortDirection.ASC
                            ? QReport.report.createdAt.asc()
                            : QReport.report.createdAt.desc();

            case PROCESSED_AT ->
                    direction == SortDirection.ASC
                            ? QReport.report.processedAt.asc()
                            : QReport.report.processedAt.desc();
        };
    }
}
