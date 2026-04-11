package com.votechori.backend.dto;

import com.votechori.backend.model.Role;
import java.time.LocalDate;

public class AuthDtos {

    public record LoginRequest(String email, String password) {}

        public record RegisterRequest(
            String name,
            String email,
            String password,
            String role,
            String aadhaarNumber,
            String dateOfBirth,
            String city,
            String state
        ) {}

    public record AuthResponse(String token, UserDto user) {}

    public record UserDto(
            Long id,
            String email,
            String name,
            Role role,
            String constituency,
            String aadhaarNumber,
            LocalDate dateOfBirth,
            String city,
            String state
    ) {}
}
