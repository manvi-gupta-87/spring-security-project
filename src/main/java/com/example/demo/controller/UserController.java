package com.example.demo.controller;

import com.example.demo.dtos.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder encoder;

    public UserController(UserDetailsManager userDetailsManager, PasswordEncoder encoder) {
        this.userDetailsManager = userDetailsManager;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest req) {
        // very basic validation
        if (req.getUserName() == null || req.getUserName().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("username and password are required");
        }

        // reject duplicates
        if (userDetailsManager.userExists(req.getUserName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("username already exists");
        }

        UserDetails userDetails = User.withUsername(req.getUserName())
                .password(encoder.encode(req.getPassword()))
                .roles("USER")
                .build();

        userDetailsManager.createUser(userDetails);
        return ResponseEntity.status(HttpStatus.OK).body("User created");
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication auth) {
        return Map.of(
                "name", auth.getName(),
                "authorities", auth.getAuthorities().toString()
        );
    }
}
