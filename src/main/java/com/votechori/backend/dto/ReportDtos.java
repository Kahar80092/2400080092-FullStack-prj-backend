package com.votechori.backend.dto;

public class ReportDtos {

    public record ReportCreateRequest(String type, String location, String description, String severity) {}
}
