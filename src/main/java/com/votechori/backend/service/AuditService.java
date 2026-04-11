package com.votechori.backend.service;

import com.votechori.backend.model.AuditLog;
import com.votechori.backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String details, String constituency, String actorEmail) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setDetails(details);
        log.setConstituency(constituency == null ? "N/A" : constituency);
        log.setActorEmail(actorEmail);
        auditLogRepository.save(log);
    }
}
