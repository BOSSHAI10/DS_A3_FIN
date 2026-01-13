package com.example.api_gateway.dtos;

import java.util.UUID;

public class AuthDTO {

    private UUID id;
    private String email;
    // Poți adăuga și alte câmpuri dacă Auth Service le returnează, dar ID-ul e critic

    public AuthDTO() {
    }

    public AuthDTO(UUID id, String email) {
        this.id = id;
        this.email = email;
    }

    // --- Getters & Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}