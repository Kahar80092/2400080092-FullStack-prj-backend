package com.votechori.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @Column(length = 20)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String party;

    @Column(nullable = false)
    private String partyShort;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String symbol;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }

    public String getPartyShort() { return partyShort; }
    public void setPartyShort(String partyShort) { this.partyShort = partyShort; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
}
