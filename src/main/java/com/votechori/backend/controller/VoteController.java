package com.votechori.backend.controller;

import com.votechori.backend.dto.VoteDtos;
import com.votechori.backend.model.AppUser;
import com.votechori.backend.repository.AppUserRepository;
import com.votechori.backend.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;
    private final AppUserRepository appUserRepository;

    public VoteController(VoteService voteService, AppUserRepository appUserRepository) {
        this.voteService = voteService;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CITIZEN','ADMIN')")
    public ResponseEntity<?> castVote(@RequestBody VoteDtos.VoteRequest request, Authentication authentication) {
        AppUser user = appUserRepository.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(voteService.castVote(user, request));
    }
}
