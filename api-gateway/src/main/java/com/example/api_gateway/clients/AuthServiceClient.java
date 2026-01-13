package com.example.api_gateway.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Component
public class AuthServiceClient {
    private final WebClient client;

    public AuthServiceClient(WebClient authClient) {
        this.client = authClient;
    }

    public Mono<UUID> createAuth(String email, String password, String role) {
        // FIX: Adăugat "/auth" în fața la "/register"
        return client.post()
                .uri("/auth/register")
                .bodyValue(new AuthPayload(email, password, role))
                .retrieve()
                .bodyToMono(UUID.class);
    }

    public Mono<Void> deleteAuth(String email) {
        // FIX: Adăugat "/auth" în fața la "/delete"
        return client.delete()
                .uri("/auth/delete/" + email)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    record AuthPayload(String email, String password, String role) {}
}