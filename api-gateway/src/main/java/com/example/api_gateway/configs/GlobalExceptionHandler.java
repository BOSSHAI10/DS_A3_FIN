package com.example.api_gateway.configs;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Object> handleValidationExceptions(WebExchangeBindException ex) {
        System.out.println("Validation error: " + ex.getMessage());
        ex.getFieldErrors().forEach(error -> 
            System.out.println("Field error: " + error.getField() + " - " + error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body("Validation failed: " + ex.getMessage());
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Object> handleServerWebInputException(ServerWebInputException ex) {
        System.out.println("ServerWebInputException: " + ex.getMessage());
        return ResponseEntity.badRequest().body("Invalid input: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        System.out.println("Generic exception: " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + ex.getMessage());
    }
}
