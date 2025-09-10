package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cors-test")
public class CorsTestController {

    // Test global CORS configuration
    @GetMapping("/global")
    public Map<String, String> testGlobalCors() {
        return Map.of(
                "message", "Global CORS configuration is working!",
                "timestamp", String.valueOf(System.currentTimeMillis())
        );
    }

    // Test endpoint-specific CORS (overrides global)
    @CrossOrigin(origins = "http://localhost:8081", maxAge = 3600)
    @GetMapping("/specific")
    public Map<String, String> testSpecificCors() {
        return Map.of(
                "message", "Endpoint-specific CORS is working!",
                "note", "This only allows localhost:8081"
        );
    }

    // Test preflight handling
    @PostMapping("/data")
    public Map<String, String> testPreflightCors(@RequestBody Map<String, String> data) {
        return Map.of(
                "received", data.toString(),
                "message", "POST with CORS successful!"
        );
    }
}
