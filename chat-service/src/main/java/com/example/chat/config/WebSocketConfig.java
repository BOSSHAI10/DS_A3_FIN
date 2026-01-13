package com.example.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Punctul de conectare: http://localhost/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // SAU "http://localhost:5173", "http://localhost"
                .withSockJS(); // Aceasta linie este CRITICĂ pentru biblioteca sockjs-client din React
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix pentru mesajele care merg CĂTRE server (ex: /app/chat)
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix pentru mesajele care vin DE LA server către client (ex: /topic/messages)
        registry.enableSimpleBroker("/topic", "/queue", "/user");

        // Prefix pentru mesaje private (dacă folosești convertAndSendToUser)
        registry.setUserDestinationPrefix("/user");
    }
}