package com.example.users.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${internal.secret}")
    private String internalSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Instanțiem filtrul manual aici pentru siguranță
        InternalSecurityFilter internalFilter = new InternalSecurityFilter(internalSecret);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Adăugăm filtrul nostru înainte de filtrul standard de autentificare
                .addFilterBefore(internalFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Permite accesul public la endpoint-urile de documentație (opțional)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // Permite POST pe /people DOAR dacă avem autoritatea ROLE_INTERNAL (setată de filtru)
                        .requestMatchers(HttpMethod.POST, "/people").hasAuthority("ROLE_INTERNAL")
                        // Orice altceva necesită autentificare
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}