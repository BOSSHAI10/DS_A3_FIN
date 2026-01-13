package com.example.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
// @ComponentScan scos - Spring Boot face asta automat pentru pachetele copil
public class UsersServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(UsersServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UsersServiceApplication.class, args);
        logger.info("Users Service started.");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}