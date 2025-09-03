package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TokenControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    public void testTokenContainsAudAndJti() {
        // Login request
        Map<String, String> loginRequest = Map.of(
                "username", "user",
                "password", "password"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/token", loginRequest, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String token = (String) response.getBody().get("access_token");
        Jwt jwt = jwtDecoder.decode(token);

        // Check aud claim
        List<String> audience = jwt.getClaimAsStringList("aud");
        assertNotNull(audience);
        assertTrue(audience.contains("demo-api"));

        // Check jti claim
        String jti = jwt.getClaimAsString("jti");
        assertNotNull(jti);
        assertFalse(jti.isEmpty());
    }

    @Test
    public void testDifferentTokensHaveDifferentJti() {
        Map<String, String> loginRequest = Map.of(
                "username", "user",
                "password", "password"
        );

        // Get first token
        ResponseEntity<Map> response1 = restTemplate.postForEntity(
                "/api/token", loginRequest, Map.class);
        String token1 = (String) response1.getBody().get("access_token");

        // Get second token
        ResponseEntity<Map> response2 = restTemplate.postForEntity(
                "/api/token", loginRequest, Map.class);
        String token2 = (String) response2.getBody().get("access_token");

        // Decode and compare JTIs
        Jwt jwt1 = jwtDecoder.decode(token1);
        Jwt jwt2 = jwtDecoder.decode(token2);

        String jti1 = jwt1.getClaimAsString("jti");
        String jti2 = jwt2.getClaimAsString("jti");

        assertNotEquals(jti1, jti2, "JTI values must be unique");
    }
}