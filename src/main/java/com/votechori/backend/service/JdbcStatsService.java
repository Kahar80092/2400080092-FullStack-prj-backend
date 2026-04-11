package com.votechori.backend.service;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class JdbcStatsService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcStatsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countVotes() {
        return countFromTable("votes");
    }

    public long countReports() {
        return countFromTable("observer_reports");
    }

    public long countAuditLogs() {
        return countFromTable("audit_logs");
    }

    public long countUsers() {
        return countFromTable("users");
    }

    public List<Map<String, Object>> getVotesWithVoterNames() {
        return jdbcTemplate.queryForList("""
            SELECT
              v.id,
              v.receipt_id AS receiptId,
              v.aadhaar_number AS aadhaarNumber,
              COALESCE(u.name, ar.name, 'Unknown') AS voterName,
              v.voter_user_id AS voterUserId,
              v.candidate_id AS candidateId,
              c.name AS candidateName,
              v.constituency,
              v.created_at AS createdAt
            FROM votes v
            LEFT JOIN users u ON u.id = v.voter_user_id
            LEFT JOIN aadhaar_records ar ON ar.aadhaar_number = v.aadhaar_number
            LEFT JOIN candidates c ON c.id = v.candidate_id
            ORDER BY v.created_at DESC
            """);
    }

    private long countFromTable(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        return count == null ? 0L : count;
    }
}
