package com.example.api_gateway.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// import lombok.Data; // Ștergem importul Lombok

// @Data // Ștergem adnotarea Lombok
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age should be greater than 18")
    private int age;

    @NotBlank(message = "Role is required")
    private String role; // "ADMIN" or "CLIENT"

    // --- Constructor implicit (obligatoriu pentru JSON parsing) ---
    public RegisterRequest() {
    }

    // --- Constructor cu toți parametrii ---
    public RegisterRequest(String name, String email, String password, int age, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.age = age;
        this.role = role;
    }

    // --- GETTERS ---
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public int getAge() {
        return age;
    }

    public String getRole() {
        return role;
    }

    // --- SETTERS ---
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // --- Metodele de tip record (dacă le foloseai ca record, adaugă și astea pt compatibilitate) ---
    // Erorile ziceau "cannot find symbol method email()", deci probabil codul tău le apela așa.

    public String name() { return name; }
    public String email() { return email; }
    public String password() { return password; }
    public int age() { return age; }
    public String role() { return role; }
}