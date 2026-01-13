package com.example.websocket.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class InternalSecurityFilter extends OncePerRequestFilter {
    
    @Value("${internal.secret}")
    private String internalSecret;
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Allow WebSocket connections without authentication
        String path = request.getRequestURI();
        return path.startsWith("/ws/chat") || path.startsWith("/ws/notifications") || path.equals("/ws/status");
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Check internal secret for API endpoints
        String requestSecret = request.getHeader("X-Internal-Secret");
        
        if (requestSecret == null || !requestSecret.equals(internalSecret)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acces interzis: Request-ul nu vine din Gateway.");
            return;
        }
        
        // Extract user identity
        String username = request.getHeader("X-Authenticated-User");
        String role = request.getHeader("X-User-Role");
        
        if (username != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username, null, Collections.singletonList(authority));
            
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
}
