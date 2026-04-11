package com.votechori.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "election_settings")
public class ElectionSetting {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private String phase = "voting";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
}
