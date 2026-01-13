package com.example.users.config;

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

        String headerSecret = request.getHeader("X-Internal-Secret");

        // IMPORTANT: Logare pentru debug (va apărea în consola Docker)
        if (headerSecret != null) {
            System.out.println("InternalSecurityFilter: Received secret: " + headerSecret);
        }

        if (internalSecret != null && !internalSecret.isEmpty() && internalSecret.equals(headerSecret)) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "system-internal",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("InternalSecurityFilter: Access GRANTED for system-internal");
        }

        filterChain.doFilter(request, response);
    }
}