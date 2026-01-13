package com.example.api_gateway.filter;

import com.example.api_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${internal.secret}")
    private String internalSecret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (validator.isSecured.test(request)) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    // Validam token-ul
                    jwtUtil.validateToken(authHeader);

                    // Extragem ID-ul userului
                    String userId = jwtUtil.extractUserId(authHeader);

                    // Modificam cererea pentru a adauga headerele interne necesare microserviciilor
                    request = exchange.getRequest()
                            .mutate()
                            .header("X-Internal-Secret", internalSecret) // Pentru InternalSecurityFilter
                            .header("X-User-Id", userId)                 // Pentru identificare user
                            .build();

                } catch (Exception e) {
                    System.out.println("Invalid access: " + e.getMessage());
                    throw new RuntimeException("Unauthorized access to application");
                }
            }

            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    public static class Config {
    }
}