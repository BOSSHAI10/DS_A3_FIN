package com.example.devices.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class InternalSecurityFilter extends OncePerRequestFilter {

    private final String internalSecret;

    // --- ACESTA ESTE CONSTRUCTORUL LIPSA CARE CAUZEAZA EROAREA ---
    public InternalSecurityFilter(String internalSecret) {
        this.internalSecret = internalSecret;
    }
    // -------------------------------------------------------------

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Citim headerele injectate de API Gateway
        String requestSecret = request.getHeader("X-Internal-Secret");
        String userId = request.getHeader("X-User-Id");

        // 2. Validam secretul intern
        if (internalSecret != null && internalSecret.equals(requestSecret)) {
            // Daca secretul e bun, autentificam requestul automat
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId != null ? userId : "system", null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. Permitem Swagger/OpenAPI fara autentificare
        String path = request.getRequestURI();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Trecem mai departe
        filterChain.doFilter(request, response);
    }
}