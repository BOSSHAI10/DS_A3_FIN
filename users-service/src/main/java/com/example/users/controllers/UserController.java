package com.example.users.controllers;

import com.example.users.dtos.UserDTO;
import com.example.users.dtos.UserDetailsDTO;
import com.example.users.dtos.UserDetailsPatchDTO;
import com.example.users.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/people")
@Validated
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getPeople() {
        return ResponseEntity.ok(userService.findUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> getUser(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDetailsDTO> getUserByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }

    // --- INSERT (Chemat de API Gateway) ---
    @PostMapping
    // @PreAuthorize nu e necesar dacă SecurityConfig se ocupă de filtrarea ROLE_INTERNAL
    public ResponseEntity<UUID> insert(@Valid @RequestBody UserDetailsDTO userDetailsDTO) {
        LOGGER.info("UserController: Received POST request to create user: {}", userDetailsDTO.getEmail());

        // Extragem ID-ul din DTO (care vine de la Gateway)
        UUID idFromGateway = userDetailsDTO.getId();

        if (idFromGateway == null) {
            LOGGER.error("UserController: ERROR - Received UserDetailsDTO with NULL ID from Gateway!");
        } else {
            LOGGER.info("UserController: Received ID from Gateway: {}", idFromGateway);
        }

        UUID createdId = userService.insert(userDetailsDTO, idFromGateway);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> partialUpdate(@PathVariable UUID id, @RequestBody UserDetailsPatchDTO patchDto) {
        // Implementare existentă...
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.remove(id);
        return ResponseEntity.noContent().build();
    }
}