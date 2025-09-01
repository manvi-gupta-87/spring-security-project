package com.example.demo.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TokenController {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder encoder;
    private final String issuer;
    private final long ttlMinutes;

    public TokenController(AuthenticationManager authManager,
                           JwtEncoder encoder,
                           @Value("${app.jwt.issuer}") String issuer,
                           @Value("${app.jwt.expires-min}") long ttlMinutes) {
        this.authenticationManager = authManager;
        this.encoder = encoder;
        this.issuer = issuer;
        this.ttlMinutes = ttlMinutes;
    }

    public static class TokenRequest {
        @NotBlank public String username;
        @NotBlank public String password;
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(@Valid @RequestBody TokenRequest req) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username, req.password)
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "error", "Unauthorized",
                    "message", "Invalid credentials"
            ));
        }

        var now = Instant.now();
        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        try {
            var claims = JwtClaimsSet.builder()
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(ttlMinutes * 60))
                    .subject(auth.getName())
                    .claim("roles", roles)
                    .build();

            var jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
            var token = encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

            return ResponseEntity.ok(Map.of(
                    "access_token", token,
                    "token_type", "Bearer",
                    "expires_in", ttlMinutes * 60,
                    "roles", roles
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "error", "JWT Encoding Error",
                    "message", e.getMessage(),
                    "details", e.getClass().getSimpleName()
            ));
        }
    }
}