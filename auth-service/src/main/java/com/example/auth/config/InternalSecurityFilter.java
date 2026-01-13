package com.example.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class InternalSecurityFilter extends OncePerRequestFilter {

    private final String internalSecret;

    public InternalSecurityFilter(String internalSecret) {
        this.internalSecret = internalSecret;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Citim header-ul secret trimis de API Gateway
        String headerSecret = request.getHeader("X-Internal-Secret");

        // 2. Verificăm dacă secretul este corect
        if (internalSecret != null && !internalSecret.isEmpty() && internalSecret.equals(headerSecret)) {
            // 3. Creăm o autentificare internă (ROLE_INTERNAL)
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "system-internal",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL"))
            );

            // 4. Setăm contextul de securitate
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. Continuăm lanțul de filtre
        filterChain.doFilter(request, response);
    }
}