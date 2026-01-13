package com.example.auth.controllers;

import com.example.auth.dtos.ChangePasswordRequest; // <--- IMPORT NOU
import com.example.auth.dtos.credentials.CredentialsDetailsDTO;
import com.example.auth.entities.Credentials;
import com.example.auth.services.CredentialsService;
import com.example.auth.services.NewUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Validated
public class RegisterController {
    private final CredentialsService credentialsService;
    private final NewUserService userService;

    public RegisterController(CredentialsService credentialsService, NewUserService userService) {
        this.credentialsService = credentialsService;
        this.userService = userService;
    }

    // 1. Endpoint pentru crearea de Credențiale (folosit de Gateway la înregistrare)
    @PostMapping("/register")
    public ResponseEntity<java.util.UUID> register(@Valid @RequestBody CredentialsDetailsDTO dto) {
        // --- MODIFICARE: Trimitem și rolul primit din DTO ---
        Credentials credentials = credentialsService.register(dto.getEmail(), dto.getPassword(), dto.getRole());
        return ResponseEntity.ok(credentials.getId());
    }

    // --- 2. ENDPOINT NOU: SCHIMBARE PAROLĂ ---
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            credentialsService.changePassword(request);
            return ResponseEntity.ok("Parolă schimbată cu succes!");
        } catch (RuntimeException e) {
            // Returnăm mesajul de eroare (ex: "Parola veche este incorectă!")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}") // Schimbăm din {email} în {id}
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable java.util.UUID id) {
        // Presupunând că în CredentialsService ai o metodă care șterge după ID
        // Dacă nu ai, poți folosi direct credentialsRepository.deleteById(id);
        credentialsService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}