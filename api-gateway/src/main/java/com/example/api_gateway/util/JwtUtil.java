package com.example.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
    }

    // --- METODELE NOI PENTRU EXTRACTIE ---

    // 1. Metoda apelata din AuthenticationFilter
    public String extractUserId(String token) {
        // De obicei 'subject' in JWT este ID-ul utilizatorului sau username-ul unic
        return extractClaim(token, Claims::getSubject);
    }

    // 2. Metoda generica de extragere a unui claim specific
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 3. Parsarea token-ului pentru a obtine toate claim-urile (payload-ul)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 4. Obtinerea cheii de semnare din secretul configurat
    private Key getSigningKey() {
        // Secretul este folosit direct ca string, nu ca BASE64
        // pentru a fi compatibil cu auth-service care folosește MACSigner(secret)
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}