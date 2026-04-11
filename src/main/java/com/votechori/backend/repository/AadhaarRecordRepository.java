package com.votechori.backend.repository;

import com.votechori.backend.model.AadhaarRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AadhaarRecordRepository extends JpaRepository<AadhaarRecord, Long> {
    Optional<AadhaarRecord> findByAadhaarNumber(String aadhaarNumber);
}
