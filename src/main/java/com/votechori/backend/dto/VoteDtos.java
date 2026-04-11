package com.votechori.backend.dto;

public class VoteDtos {

    public record VoteRequest(String aadhaarNumber, String candidateId, String constituency) {}

    public record VoteResponse(boolean success, String receiptId, String message) {}
}
