package com.votechori.backend.config;

import com.votechori.backend.model.*;
import com.votechori.backend.repository.*;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final AadhaarRecordRepository aadhaarRecordRepository;
    private final CandidateRepository candidateRepository;
    private final ElectionSettingRepository electionSettingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository appUserRepository,
                      AadhaarRecordRepository aadhaarRecordRepository,
                      CandidateRepository candidateRepository,
                      ElectionSettingRepository electionSettingRepository,
                      PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.aadhaarRecordRepository = aadhaarRecordRepository;
        this.candidateRepository = candidateRepository;
        this.electionSettingRepository = electionSettingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedAadhaar();
        seedCandidates();
        seedElectionSettings();
    }

    private void seedUsers() {
        if (appUserRepository.count() > 0) {
            return;
        }

        appUserRepository.saveAll(List.of(
            user("admin@eci.gov.in", "admin123", "System Administrator", Role.ADMIN,
                "123456789001", "1988-01-15", "New Delhi", "Delhi"),
            user("observer@eci.gov.in", "observer123", "Ramesh Observer", Role.OBSERVER,
                "123456789002", "1989-03-22", "New Delhi", "Delhi"),
            user("analyst@eci.gov.in", "analyst123", "Dr. Anjali Data", Role.ANALYST,
                "123456789003", "1990-07-10", "New Delhi", "Delhi"),
            user("citizen@example.com", "citizen123", "Rahul Kumar", Role.CITIZEN,
                "123456789012", "1998-05-15", "New Delhi", "Delhi")
        ));
    }

        private AppUser user(String email,
                 String password,
                 String name,
                 Role role,
                 String aadhaar,
                 String dob,
                 String city,
                 String state) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setName(name);
        user.setRole(role);
        user.setAadhaarNumber(aadhaar);
        user.setDateOfBirth(java.time.LocalDate.parse(dob));
        user.setCity(city);
        user.setState(state);
        user.setConstituency(city + ", " + state);
        return user;
    }

    private void seedAadhaar() {
        if (aadhaarRecordRepository.count() > 0) {
            return;
        }

        aadhaarRecordRepository.saveAll(List.of(
                aadhaar("123456789012", "Rahul Kumar", "1998-05-15", "Delhi", "Delhi-Central"),
                aadhaar("234567890123", "Priya Sharma", "1991-08-22", "Maharashtra", "Mumbai-North"),
                aadhaar("456789012345", "Sneha Reddy", "2002-11-28", "Telangana", "Hyderabad-Central")
        ));
    }

    private AadhaarRecord aadhaar(String no, String name, String dob, String state, String constituency) {
        AadhaarRecord record = new AadhaarRecord();
        record.setAadhaarNumber(no);
        record.setName(name);
        record.setDob(dob);
        record.setState(state);
        record.setConstituency(constituency);
        return record;
    }

    private void seedCandidates() {
        List<Candidate> candidates = List.of(
                candidate("BJP", "Rajesh Kumar Singh", "Bharatiya Janata Party", "BJP", "#FF9933", "🪷"),
                candidate("INC", "Priya Gandhi Sharma", "Indian National Congress", "INC", "#00BFFF", "✋"),
                candidate("AAP", "Arvind Kejriwal", "Aam Aadmi Party", "AAP", "#0F6CBD", "🧹"),
                candidate("BSP", "Mayawati Devi", "Bahujan Samaj Party", "BSP", "#1E3A8A", "🐘"),
                candidate("SP", "Akhilesh Pratap", "Samajwadi Party", "SP", "#DC2626", "🚲"),
                candidate("TMC", "Mamata Roy", "All India Trinamool Congress", "TMC", "#16A34A", "🌸"),
                candidate("CPM", "Sitaram Das", "Communist Party of India (Marxist)", "CPI(M)", "#B91C1C", "⚒️"),
                candidate("DMK", "Muthuvel Kannan", "Dravida Munnetra Kazhagam", "DMK", "#111827", "☀️"),
                candidate("BJD", "Naveen Patnaik Jr.", "Biju Janata Dal", "BJD", "#15803D", "🐚"),
                candidate("NCP", "Sharad Pawar Jr.", "Nationalist Congress Party", "NCP", "#1D4ED8", "⏰"),
                candidate("NOTA", "NOTA", "None of the Above", "NOTA", "#6b7280", "❌")
        );

        for (Candidate candidate : candidates) {
            candidateRepository.save(candidate);
        }
    }

    private Candidate candidate(String id, String name, String party, String partyShort, String color, String symbol) {
        Candidate candidate = new Candidate();
        candidate.setId(id);
        candidate.setName(name);
        candidate.setParty(party);
        candidate.setPartyShort(partyShort);
        candidate.setColor(color);
        candidate.setSymbol(symbol);
        return candidate;
    }

    private void seedElectionSettings() {
        if (electionSettingRepository.findById(1L).isPresent()) {
            return;
        }

        ElectionSetting setting = new ElectionSetting();
        setting.setId(1L);
        setting.setPhase("voting");
        electionSettingRepository.save(setting);
    }
}
