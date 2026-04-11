package com.votechori.backend.controller;

import com.votechori.backend.model.AadhaarRecord;
import com.votechori.backend.model.AppUser;
import com.votechori.backend.repository.AadhaarRecordRepository;
import com.votechori.backend.repository.AppUserRepository;
import com.votechori.backend.repository.CandidateRepository;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final CandidateRepository candidateRepository;
    private final AadhaarRecordRepository aadhaarRecordRepository;
    private final AppUserRepository appUserRepository;

    public PublicController(CandidateRepository candidateRepository,
                            AadhaarRecordRepository aadhaarRecordRepository,
                            AppUserRepository appUserRepository) {
        this.candidateRepository = candidateRepository;
        this.aadhaarRecordRepository = aadhaarRecordRepository;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/candidates")
    public ResponseEntity<?> candidates() {
        return ResponseEntity.ok(candidateRepository.findAll());
    }

    @GetMapping("/aadhaar/{aadhaarNumber}")
    public ResponseEntity<?> verifyAadhaar(@PathVariable String aadhaarNumber) {
        return aadhaarRecordRepository.findByAadhaarNumber(aadhaarNumber)
            .<ResponseEntity<?>>map(record -> ResponseEntity.ok(Map.of(
                "aadhaarNumber", record.getAadhaarNumber(),
                "name", record.getName(),
                "dob", record.getDob(),
                "state", record.getState(),
                "constituency", record.getConstituency()
            )))
            .orElseGet(() -> appUserRepository.findByAadhaarNumber(aadhaarNumber)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(userAadhaarPayload(user)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Aadhaar not found"))));
    }

        private Map<String, Object> userAadhaarPayload(AppUser user) {
        String dob = user.getDateOfBirth() == null ? "" : user.getDateOfBirth().toString();
        String state = user.getState() == null ? "" : user.getState();
        String constituency = user.getConstituency();
        if ((constituency == null || constituency.isBlank()) && user.getCity() != null && !user.getCity().isBlank()) {
            constituency = user.getCity();
        }

        return Map.of(
            "aadhaarNumber", user.getAadhaarNumber(),
            "name", user.getName(),
            "dob", dob,
            "state", state,
            "constituency", constituency == null ? "" : constituency
        );
        }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
