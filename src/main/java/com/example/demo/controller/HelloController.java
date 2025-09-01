package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello User - Welcome to our application!";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/health")
    public String health() {
        return "Application is running!";
    }
}
