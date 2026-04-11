package com.votechori.backend.service;

import com.votechori.backend.dto.AuthDtos;
import com.votechori.backend.model.AadhaarRecord;
import com.votechori.backend.model.AppUser;
import com.votechori.backend.model.Role;
import com.votechori.backend.repository.AadhaarRecordRepository;
import com.votechori.backend.repository.AppUserRepository;
import com.votechori.backend.security.JwtService;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final AadhaarRecordRepository aadhaarRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(AppUserRepository appUserRepository,
                       AadhaarRecordRepository aadhaarRecordRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       AuditService auditService) {
        this.appUserRepository = appUserRepository;
        this.aadhaarRecordRepository = aadhaarRecordRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.email() == null ? "" : request.email().toLowerCase().trim();
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        String aadhaar = request.aadhaarNumber() == null ? "" : request.aadhaarNumber().trim();
        if (!aadhaar.matches("^\\d{12}$")) {
            throw new IllegalArgumentException("Aadhaar number must be exactly 12 digits");
        }
        if (appUserRepository.existsByAadhaarNumber(aadhaar)) {
            throw new IllegalArgumentException("Aadhaar is already registered");
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(request.dateOfBirth());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Date of birth must be in YYYY-MM-DD format");
        }

        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException("You must be at least 18 years old to register");
        }

        String city = request.city() == null ? "" : request.city().trim();
        String state = request.state() == null ? "" : request.state().trim();
        if (city.isBlank() || state.isBlank()) {
            throw new IllegalArgumentException("City and state are required");
        }

        Role role = parseRoleOrDefault(request.role(), Role.CITIZEN);
        if (role == Role.OBSERVER) {
            role = Role.ADMIN;
        }

        AppUser user = new AppUser();
        user.setName(request.name());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setAadhaarNumber(aadhaar);
        user.setDateOfBirth(dob);
        user.setCity(city);
        user.setState(state);
        user.setConstituency(city + ", " + state);
        AppUser saved = appUserRepository.save(user);

        if (aadhaarRecordRepository.findByAadhaarNumber(aadhaar).isEmpty()) {
            AadhaarRecord record = new AadhaarRecord();
            record.setAadhaarNumber(aadhaar);
            record.setName(saved.getName());
            record.setDob(dob.toString());
            record.setState(state);
            record.setConstituency(saved.getConstituency());
            aadhaarRecordRepository.save(record);
        }

        var springUser = org.springframework.security.core.userdetails.User
                .withUsername(saved.getEmail())
                .password(saved.getPasswordHash())
                .authorities("ROLE_" + saved.getRole().name())
                .build();

        String token = jwtService.generateToken(springUser);
        auditService.log("USER_REGISTER", "New user registered", "N/A", saved.getEmail());
        return new AuthDtos.AuthResponse(token, toUserDto(saved));
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        var springUser = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String token = jwtService.generateToken(springUser);
        auditService.log("USER_LOGIN", "User logged in", user.getConstituency(), user.getEmail());
        return new AuthDtos.AuthResponse(token, toUserDto(user));
    }

    public AuthDtos.UserDto toUserDto(AppUser user) {
        return new AuthDtos.UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getConstituency(),
                user.getAadhaarNumber(),
                user.getDateOfBirth(),
                user.getCity(),
                user.getState()
        );
    }

    private Role parseRoleOrDefault(String value, Role defaultRole) {
        if (value == null || value.isBlank()) {
            return defaultRole;
        }
        try {
            return Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return defaultRole;
        }
    }
}
