package com.example.auth.repositories;

import com.example.auth.entities.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CredentialsRepository extends JpaRepository<Credentials, UUID> {

    // Spring face automat query-ul, nu e nevoie să îl scrii tu manual
    Optional<Credentials> findByEmail(String email);

    boolean existsByEmail(String email);

    // Aceasta este linia importantă pentru ștergere
    void deleteByEmail(String email);
}
