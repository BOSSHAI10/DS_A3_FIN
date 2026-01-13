package com.example.api_gateway.services;

import com.example.api_gateway.clients.AuthServiceClient;
import com.example.api_gateway.clients.UsersServiceClient;
import com.example.api_gateway.dtos.RegisterRequest;
import com.example.api_gateway.idempotency.IdempotencyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class RegisterOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterOrchestrator.class);

    private final AuthServiceClient auth;
    private final UsersServiceClient users;
    private final IdempotencyStore idem;

    public RegisterOrchestrator(AuthServiceClient auth, UsersServiceClient users, IdempotencyStore idem) {
        this.auth = auth;
        this.users = users;
        this.idem = idem;
    }

    public Mono<Integer> register(RegisterRequest req, String idempotencyKey) {
        LOGGER.info("RegisterOrchestrator: Se procesează înregistrarea pentru: {}", req.getEmail());

        String role = (req.getRole() != null && !req.getRole().isEmpty()) ? req.getRole() : "USER";

        if (idempotencyKey != null && idem.contains(idempotencyKey)) {
            return Mono.just(idem.get(idempotencyKey));
        }

        // Pas 1: Creăm în Auth și primim UUID
        return auth.createAuth(req.getEmail(), req.getPassword(), role)
                .flatMap(generatedId -> {
                    LOGGER.info("Auth a generat ID-ul: {}. Îl trimitem la Users Service...", generatedId);

                    // Pas 2: Creăm în Users cu același ID
                    return users.createUser(
                            req.getEmail(),
                            req.getName(),
                            req.getAge(),
                            role,
                            generatedId
                    );
                })
                .then(Mono.fromSupplier(() -> {
                    if (idempotencyKey != null) idem.set(idempotencyKey, 201);
                    return 201;
                }))
                .onErrorResume(ex -> {
                    LOGGER.error("Eroare în orchestrator: {}", ex.getMessage());
                    // Rollback (ștergere din Auth dacă Users eșuează)
                    return auth.deleteAuth(req.getEmail())
                            .onErrorResume(ignore -> Mono.empty())
                            .then(Mono.error(ex));
                });
    }
}