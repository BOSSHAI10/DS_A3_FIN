package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// ADUGĂM ASTA OBLIGATORIU:
@ComponentScan(basePackages = {"com.example.auth", "com.example.auth.config", "com.example.auth.services", "com.example.auth.controllers", "com.example.auth.repositories", "com.example.auth.handlers"})
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}