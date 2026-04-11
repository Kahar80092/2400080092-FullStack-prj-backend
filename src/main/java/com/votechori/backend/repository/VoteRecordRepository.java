package com.votechori.backend.repository;

import com.votechori.backend.model.VoteRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRecordRepository extends JpaRepository<VoteRecord, Long> {
    boolean existsByAadhaarNumber(String aadhaarNumber);
    Optional<VoteRecord> findByReceiptId(String receiptId);
}
