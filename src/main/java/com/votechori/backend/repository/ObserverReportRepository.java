package com.votechori.backend.repository;

import com.votechori.backend.model.ObserverReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObserverReportRepository extends JpaRepository<ObserverReport, Long> {
    List<ObserverReport> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
}
