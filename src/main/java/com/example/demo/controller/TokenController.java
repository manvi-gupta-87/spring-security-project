package com.example.demo.controller;

import com.example.demo.dtos.RefreshTokenRequest;
import com.example.demo.model.RefreshToken;
import com.example.demo.service.RefreshTokenService;
import com.example.demo.service.TokenBlacklistService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.el.parser.Token;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TokenController {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder encoder;
    private final String issuer;
    private final long ttlMinutes;
    private final String audience;
    private final JwtDecoder decoder;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService blacklistService;


    public TokenController(AuthenticationManager authManager,
                           JwtEncoder encoder,
                           JwtDecoder decoder,
                           RefreshTokenService refreshTokenService,
                           TokenBlacklistService blacklistService,
                           @Value("${app.jwt.issuer}") String issuer,
                           @Value("${app.jwt.expires-min}") long ttlMinutes,
                           @Value("${app.jwt.audience}") String audience) {
        this.authenticationManager = authManager;
        this.encoder = encoder;
        this.decoder = decoder;
        this.issuer = issuer;
        this.blacklistService = blacklistService;
        this.ttlMinutes = ttlMinutes;
        this.audience = audience;
        this.refreshTokenService = refreshTokenService;
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
                    .claim("aud", List.of(audience))
                    .claim("jti", UUID.randomUUID().toString())
                    .build();

//            var jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
            var jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();
            var token = encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

            //Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(auth.getName());

            return ResponseEntity.ok(Map.of(
                    "access_token", token,
                    "refresh_token", refreshToken.getToken(),
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

    /** Endpoint that takes current valid token and generates a new token with extended validity
     * @AuthenticationPrincipal annotation indicates Spring to provide the current user's token
     * **/
    @PostMapping("/token/refresh")
    public ResponseEntity<?>  refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        if (request == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "status" ,401,
                    "error","Unauthorized",
                    "message", "Valid Token Required"
            ));
        }

        // Verify and rotate the refresh token
        RefreshToken newRefreshToken = refreshTokenService.verifyAndRotateToken(request.refresh_token);

        String userName = newRefreshToken.getUsername();
        // You'll need to load user roles - either from DB or store in refresh token
        // For now, using a simple approach (you should load from UserDetailsService)
        List<String> roles = List.of("ROLE_USER"); // Load actual roles

        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlMinutes* 60))
                .claim("roles", roles)
                .claim("aud", List.of(audience))
                .claim("jti", UUID.randomUUID().toString())
                .build();

        // Creates actual New token using algorithm HS256
//        var jwsheader = JwsHeader.with(MacAlgorithm.HS256).build();
        var jwsheader = JwsHeader.with(SignatureAlgorithm.RS256).build();
        var token = encoder.encode(JwtEncoderParameters.from(jwsheader, claims)).getTokenValue();

        return ResponseEntity.status(200).body(Map.of(
                "access_token", token,
                "refresh_token",newRefreshToken.getToken(),
                "token_type", "Bearer",
                "expires_in", ttlMinutes * 60,
                "roles", roles,
                "refreshed_at", now.toString()
        ));
    }

    @PostMapping("/logout")
    ResponseEntity<?>logout(@AuthenticationPrincipal Jwt jwt,
                            @RequestHeader(value = "X-Refresh-Token", required = false) String
                                    refreshToken) {

        Map<String, Object> response = new HashMap<>();
        try {
            // Blacklist the access token JTI
            if (jwt != null) {
                String jti = jwt.getClaimAsString("jti");
                Instant expiresAt = jwt.getExpiresAt();
                if (jti != null && expiresAt != null) {
                    blacklistService.blackListToken(jti, expiresAt);
                    response.put("access_token_revoked", true);
                }
            }

            //Invalidate refresh token
            if (refreshToken != null) {
                try {
                    Optional<RefreshToken> refreshToken1 = refreshTokenService.findByToken(refreshToken);
                    if (refreshToken1.isPresent()) {
                        refreshTokenService.invalidateTokenFamily(refreshToken1.get().getTokenFamily());
                        response.put("refresh_token_revoked", true);
                    }
                } catch (Exception e) {
                    response.put("refresh_token_error", e.getMessage());
                }
            }
            response.put("message", "Successfully logged out");
            response.put("timestamp", Instant.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Logout failed",
                    "message", e.getMessage()
            ));
        }
    }
}