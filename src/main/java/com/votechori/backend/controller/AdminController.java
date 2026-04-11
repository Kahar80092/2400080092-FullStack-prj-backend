package com.votechori.backend.controller;

import com.votechori.backend.model.ElectionSetting;
import com.votechori.backend.repository.AuditLogRepository;
import com.votechori.backend.repository.ElectionSettingRepository;
import com.votechori.backend.repository.ObserverReportRepository;
import com.votechori.backend.repository.VoteRecordRepository;
import com.votechori.backend.service.AuditService;
import com.votechori.backend.service.JdbcStatsService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN','OBSERVER')")
public class AdminController {

    private final VoteRecordRepository voteRecordRepository;
    private final ObserverReportRepository observerReportRepository;
    private final AuditLogRepository auditLogRepository;
    private final ElectionSettingRepository electionSettingRepository;
    private final AuditService auditService;
    private final JdbcStatsService jdbcStatsService;

    public AdminController(VoteRecordRepository voteRecordRepository,
                           ObserverReportRepository observerReportRepository,
                           AuditLogRepository auditLogRepository,
                           ElectionSettingRepository electionSettingRepository,
                           AuditService auditService,
                           JdbcStatsService jdbcStatsService) {
        this.voteRecordRepository = voteRecordRepository;
        this.observerReportRepository = observerReportRepository;
        this.auditLogRepository = auditLogRepository;
        this.electionSettingRepository = electionSettingRepository;
        this.auditService = auditService;
        this.jdbcStatsService = jdbcStatsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        ElectionSetting setting = electionSettingRepository.findById(1L).orElseGet(() -> {
            ElectionSetting s = new ElectionSetting();
            s.setId(1L);
            s.setPhase("voting");
            return electionSettingRepository.save(s);
        });

        return ResponseEntity.ok(Map.of(
            "totalVotesCast", jdbcStatsService.countVotes(),
            "reportsSubmitted", jdbcStatsService.countReports(),
            "auditCount", jdbcStatsService.countAuditLogs(),
                "electionPhase", setting.getPhase()
        ));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> reports() {
        return ResponseEntity.ok(observerReportRepository.findAll());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> auditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }

    @GetMapping("/votes")
    public ResponseEntity<?> votes() {
        return ResponseEntity.ok(jdbcStatsService.getVotesWithVoterNames());
    }

    @PatchMapping("/phase")
    public ResponseEntity<?> updatePhase(@RequestBody Map<String, String> body, Authentication authentication) {
        String phase = body.getOrDefault("phase", "voting");
        ElectionSetting setting = electionSettingRepository.findById(1L).orElseGet(ElectionSetting::new);
        setting.setId(1L);
        setting.setPhase(phase);
        electionSettingRepository.save(setting);
        auditService.log("ELECTION_PHASE_CHANGE", "Phase changed to " + phase, "ALL", authentication.getName());
        return ResponseEntity.ok(Map.of("phase", phase));
    }
}
