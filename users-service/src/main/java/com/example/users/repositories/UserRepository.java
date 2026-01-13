package com.example.users.repositories;

import com.example.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Metoda standard pentru a găsi după email (folosită la login/verificări)
    Optional<User> findByEmail(String email);

    // Metoda pentru a verifica existența (folosită la validări)
    boolean existsByEmail(String email);

    // Notă:
    // 1. findById(UUID id) este oferită automat de JpaRepository.
    // 2. existsById(UUID id) este oferită automat de JpaRepository.
    // 3. save(User user) este oferită automat și face INSERT sau UPDATE.
    //    Deci nu avem nevoie de query-uri manuale de UPDATE.
}