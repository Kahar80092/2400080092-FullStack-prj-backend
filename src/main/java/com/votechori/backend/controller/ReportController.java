package com.votechori.backend.controller;

import com.votechori.backend.dto.ReportDtos;
import com.votechori.backend.model.AppUser;
import com.votechori.backend.model.ObserverReport;
import com.votechori.backend.model.ReportStatus;
import com.votechori.backend.repository.AppUserRepository;
import com.votechori.backend.repository.ObserverReportRepository;
import com.votechori.backend.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ObserverReportRepository observerReportRepository;
    private final AppUserRepository appUserRepository;
    private final AuditService auditService;

    public ReportController(ObserverReportRepository observerReportRepository,
                            AppUserRepository appUserRepository,
                            AuditService auditService) {
        this.observerReportRepository = observerReportRepository;
        this.appUserRepository = appUserRepository;
        this.auditService = auditService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OBSERVER')")
    public ResponseEntity<?> create(@RequestBody ReportDtos.ReportCreateRequest request, Authentication authentication) {
        AppUser user = appUserRepository.findByEmail(authentication.getName()).orElseThrow();

        ObserverReport report = new ObserverReport();
        report.setType(request.type());
        report.setLocation(request.location());
        report.setDescription(request.description());
        report.setSeverity(request.severity());
        report.setStatus(ReportStatus.PENDING);
        report.setReporterId(user.getId());
        report.setReporterName(user.getName());

        ObserverReport saved = observerReportRepository.save(report);
        auditService.log("REPORT_SUBMITTED", "Observer/admin report submitted", user.getConstituency(), user.getEmail());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('ADMIN','OBSERVER')")
    public ResponseEntity<?> mine(Authentication authentication) {
        AppUser user = appUserRepository.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(observerReportRepository.findByReporterIdOrderByCreatedAtDesc(user.getId()));
    }
}
