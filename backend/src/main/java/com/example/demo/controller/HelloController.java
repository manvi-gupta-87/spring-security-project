package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello User - Welcome to our application!";
    }

    @GetMapping("/health")
    public String health() {
        return "Application is running!";
    }
    
    /**
     * Demo endpoint to show SecurityContext details
     * This demonstrates how Spring Security stores authentication information
     */
    @GetMapping("/api/security-context")
    public Map<String, Object> getSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> context = new HashMap<>();
        
        if (auth != null) {
            context.put("authenticated", auth.isAuthenticated());
            context.put("principal", auth.getPrincipal().toString());
            context.put("credentials", auth.getCredentials() != null ? "[PROTECTED]" : null);
            context.put("authorities", auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList()));
            context.put("details", auth.getDetails());
            context.put("principalClass", auth.getPrincipal().getClass().getSimpleName());
            context.put("authenticationClass", auth.getClass().getSimpleName());
        } else {
            context.put("authenticated", false);
            context.put("message", "No authentication found in SecurityContext");
        }
        
        // Show how SecurityContext is stored
        context.put("contextHolderStrategy", SecurityContextHolder.getContextHolderStrategy().getClass().getSimpleName());
        context.put("contextClass", SecurityContextHolder.getContext().getClass().getSimpleName());
        
        return context;
    }
    
    /**
     * Demo endpoint for basic authentication
     * Shows that both Basic Auth and JWT can work together
     */
    @GetMapping("/api/basic-auth-demo")
    public Map<String, Object> basicAuthDemo(Authentication auth) {
        return Map.of(
                "message", "Successfully authenticated with Basic Auth",
                "username", auth.getName(),
                "authorities", auth.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .collect(Collectors.toList()),
                "authenticationType", auth.getClass().getSimpleName()
        );
    }
}
