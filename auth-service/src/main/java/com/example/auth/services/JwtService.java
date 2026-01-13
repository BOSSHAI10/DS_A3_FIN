package com.example.auth.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationInMillis;

    public String generateToken(UUID userId, String email, String role) {
        try {
            // 1. Creăm semnătura HMAC cu secretul nostru
            JWSSigner signer = new MACSigner(secret);

            // 2. Pregătim claim-urile (informațiile din token)
            Instant now = Instant.now();
            long expirationTimeSec = now.getEpochSecond() + (expirationInMillis / 1000);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(email)                         // "sub": email-ul utilizatorului
                    .claim("userId", userId.toString())     // "userId": ID-ul
                    .claim("role", role)                    // "role": Rolul (CLIENT/ADMIN)
                    .issueTime(Date.from(now))              // "iat": momentul emiterii
                    .expirationTime(Date.from(Instant.ofEpochSecond(expirationTimeSec))) // "exp": expirarea în secunde Unix
                    .build();

            // 4. Creăm obiectul JWT semnat (Header + Payload)
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            // 5. Aplicăm semnătura
            signedJWT.sign(signer);

            // 6. Returnăm token-ul serializat
            return signedJWT.serialize();

        } catch (JOSEException e) {
            // Este bună practica să loghezi eroarea aici înainte de a arunca excepția
            throw new RuntimeException("Eroare la generarea token-ului JWT", e);
        }
    }
}