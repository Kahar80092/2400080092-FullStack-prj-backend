package com.votechori.backend.service;

import com.votechori.backend.dto.VoteDtos;
import com.votechori.backend.model.AppUser;
import com.votechori.backend.model.VoteRecord;
import com.votechori.backend.repository.VoteRecordRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VoteService {

    private final VoteRecordRepository voteRecordRepository;
    private final AuditService auditService;

    public VoteService(VoteRecordRepository voteRecordRepository, AuditService auditService) {
        this.voteRecordRepository = voteRecordRepository;
        this.auditService = auditService;
    }

    public VoteDtos.VoteResponse castVote(AppUser user, VoteDtos.VoteRequest request) {
        if (voteRecordRepository.existsByAadhaarNumber(request.aadhaarNumber())) {
            return new VoteDtos.VoteResponse(false, null, "This Aadhaar has already voted.");
        }

        VoteRecord vote = new VoteRecord();
        vote.setReceiptId("RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        vote.setAadhaarNumber(request.aadhaarNumber());
        vote.setCandidateId(request.candidateId());
        vote.setConstituency(request.constituency());
        vote.setVoterUserId(user.getId());

        VoteRecord saved = voteRecordRepository.save(vote);
        auditService.log("VOTE_CAST", "Vote cast for candidate " + request.candidateId(), request.constituency(), user.getEmail());
        return new VoteDtos.VoteResponse(true, saved.getReceiptId(), "Vote cast successfully.");
    }
}
