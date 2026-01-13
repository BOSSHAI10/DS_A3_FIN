package com.example.api_gateway.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class UsersServiceClient {
    private final WebClient client;

    // 1. Injectăm secretul din application.properties
    @Value("${internal.secret}")
    private String internalSecret;

    public UsersServiceClient(WebClient usersClient) {
        this.client = usersClient;
    }

    public Mono<Void> createUser(String email, String name, int age, String role, UUID id) {
        // Logăm pentru debug să fim siguri că se apelează
        System.out.println("Gateway: Sending POST to Users Service with ID: " + id + " and Secret: " + internalSecret);

        return client.post()
                .uri("/people")
                // 2. ADĂUGĂM HEADER-UL EXPLICIT AICI
                .header("X-Internal-Secret", internalSecret)
                .bodyValue(new UserPayload(id, name, email, age, role))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), resp -> resp.createException())
                .onStatus(s -> s.is5xxServerError(), resp -> resp.createException())
                .toBodilessEntity()
                .then();
    }

    record UserPayload(UUID id, String name, String email, int age, String role) {}
}