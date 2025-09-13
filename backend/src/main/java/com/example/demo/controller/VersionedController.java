package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VersionedController {

    // Version 1 endpoints
    @GetMapping("/api/v1/users")
    public Map<String, Object> getUsersV1() {
        return Map.of(
                "version", "1.0",
                "users", Map.of(
                        "name", "John Doe",
                        "email", "john@example.com"
                )
        );
    }

    // Version 2 endpoints - with breaking changes
    @GetMapping("/api/v2/users")
    public Map<String, Object> getUsersV2() {
        return Map.of(
                "version", "2.0",
                "data", Map.of(
                        "firstName", "John",
                        "lastName", "Doe",
                        "emailAddress", "john@example.com",
                        "phoneNumber", "+1-234-567-8900"
                ),
                "metadata", Map.of(
                        "apiVersion", "2.0",
                        "timestamp", System.currentTimeMillis()
                )
        );
    }

    // Version info endpoint
    @GetMapping("/api/versions")
    public Map<String, Object> getVersions() {
        return Map.of(
                "supportedVersions", new String[]{"v1", "v2"},
                "currentVersion", "v2",
                "deprecatedVersions", new String[]{"v1"},
                "deprecationDate", "2025-12-31",
                "migrationGuide", "https://api.example.com/migration-guide"
        );
    }
}
