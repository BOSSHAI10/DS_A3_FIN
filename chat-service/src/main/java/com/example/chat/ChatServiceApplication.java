package com.example.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
// ADUGĂM ASTA: Îi spunem explicit unde să caute configurările
@ComponentScan(basePackages = {
                "com.example.chat",
                "com.example.chat.config",
                "com.example.chat.services",
                "com.example.chat.controllers",
                "com.example.chat.repositories",
                "com.example.chat.consumer"
        })
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}