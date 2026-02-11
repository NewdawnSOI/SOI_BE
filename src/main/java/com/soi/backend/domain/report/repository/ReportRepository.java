package com.soi.backend.domain.report.repository;

import com.soi.backend.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReportRepository extends JpaRepository<Report, Long> , ReportQueryRepository {
}
