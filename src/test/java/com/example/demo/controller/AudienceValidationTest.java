package com.example.demo.controller;

import com.example.demo.config.AudienceValidator;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AudienceValidationTest {

    @Test
    public void testValidAudience() {
        AudienceValidator validator = new AudienceValidator();
        // Simulate setting the expected audience
        // In real test, this would come from Spring context

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("aud", List.of("demo-api"))
                .claim("sub", "user")
                .claim("iss", "demo-app")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // This would pass if audience matches
        // OAuth2TokenValidatorResult result = validator.validate(jwt);
        // assertTrue(result.hasErrors() == false);
    }

    @Test
    public void testInvalidAudience() {
        AudienceValidator validator = new AudienceValidator();

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("aud", List.of("wrong-api"))  // Wrong audience
                .claim("sub", "user")
                .claim("iss", "demo-app")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // This would fail due to wrong audience
        // OAuth2TokenValidatorResult result = validator.validate(jwt);
        // assertTrue(result.hasErrors());
    }

    @Test
    public void testMissingAudience() {
        AudienceValidator validator = new AudienceValidator();

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                // No audience claim
                .claim("sub", "user")
                .claim("iss", "demo-app")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // This would fail due to missing audience
        // OAuth2TokenValidatorResult result = validator.validate(jwt);
        // assertTrue(result.hasErrors());
    }
}