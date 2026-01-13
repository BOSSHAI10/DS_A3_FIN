package com.example.auth.controllers;

import com.example.auth.entities.Credentials;
import com.example.auth.repositories.CredentialsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; // Import esențial!
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class CredentialsController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialsController.class);

    private final CredentialsRepository credentialsRepository;

    public CredentialsController(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Credentials>> getAllCredentials() {
        return ResponseEntity.ok(credentialsRepository.findAll());
    }

    // --- ENDPOINT PENTRU ȘTERGERE (Adăugat Aici) ---
    // În CredentialsController.java

    @DeleteMapping("/{email}")
    @Transactional // <--- FĂRĂ ASTA NU SE ȘTERGE DIN BAZA DE DATE
    public ResponseEntity<String> deleteUserByEmail(@PathVariable String email) {
        if (credentialsRepository.existsByEmail(email)) {
            credentialsRepository.deleteByEmail(email);
            return ResponseEntity.ok("User sters din Auth.");
        }
        return ResponseEntity.status(404).body("Email negasit.");
    }

    // --- RESTORE ---
    @PostMapping("/restore")
    public ResponseEntity<String> restoreCredentials(@RequestBody List<Credentials> credentialsList) {
        int restoredCount = 0;
        int skippedCount = 0;

        for (Credentials c : credentialsList) {
            try {
                if (!credentialsRepository.existsByEmail(c.getEmail())) {
                    credentialsRepository.save(c);
                    restoredCount++;
                } else {
                    skippedCount++;
                }
            } catch (Exception e) {
                logger.error("Eroare la importul userului: {}", c.getEmail());
            }
        }
        return ResponseEntity.ok("Restaurare: " + restoredCount + " adăugați, " + skippedCount + " ignorați.");
    }
}