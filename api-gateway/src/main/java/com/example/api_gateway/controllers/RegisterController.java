package com.example.api_gateway.controllers;


import com.example.api_gateway.dtos.RegisterRequest;
import com.example.api_gateway.services.RegisterOrchestrator;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);

    private final RegisterOrchestrator orchestrator;
    public RegisterController(RegisterOrchestrator orchestrator) { this.orchestrator = orchestrator; }

    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(
            @Valid @RequestBody RegisterRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey) {

        LOGGER.info("Register request received: email={}, name={}, age={}, role={}", req.email(), req.name(), req.age(), req.role());
        System.out.println("Register request received: " + req.email());

        if (req.age() < 18) {
            LOGGER.warn("User under 18 attempted registration: {}", req.email());
            return Mono.just(ResponseEntity.badRequest().body("User must be at least 18 years old"));
        }

        return orchestrator.register(req, idemKey)
                .map(code -> ResponseEntity.status(code).build())
                .onErrorResume(ex -> {
                    System.out.println("Registration error: " + ex.getMessage());
                    ex.printStackTrace();
                    return Mono.just(ResponseEntity.status(500).body("Error: " + ex.getMessage()));
                });
    }
}
