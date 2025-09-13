package com.example.demo.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/validation")
@Validated
public class ValidationController {

    // Inner class for user registeration dto
    public static class UserRegistrationDto {
        @NotBlank(message = "userName is correct")
        @Size(min = 3, max = 20, message = "UserName must be between 3 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        private String userName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Password must contain at least one uppercase, one lowercase, and one digit")
        private String password;

        @Min(value = 18, message = "Must be at least 18 years old")
        @Max(value = 120, message = "Invalid age")
        private int age;

        // Getters and setters
        public String getUsername() {
            return userName;
        }

        public void setUsername(String username) {
            this.userName = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto dto) {
        // Validation happens automatically via @Valid
        // If validation fails, Spring returns 400 with error details

        return ResponseEntity.ok(Map.of(
                "message", "Registration successful",
                "username", dto.getUsername(),
                "email", dto.getEmail()
        ));
    }
    // Path variable validation
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUser(
            @PathVariable
            @Min(value = 1, message = "User ID must be positive")
            Long id) {
        return ResponseEntity.ok(Map.of("userId", id, "status", "found"));
    }

    // Query parameter validation
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam
            @NotBlank(message = "Search query cannot be empty")
            @Size(min = 2, max = 100, message = "Search query must be 2-100 characters")
            String query) {
        return ResponseEntity.ok(Map.of("query", query, "results", "..."));
    }

}
