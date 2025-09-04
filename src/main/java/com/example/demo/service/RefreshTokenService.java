package com.example.demo.service;

import com.example.demo.model.RefreshToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Complete Flow Example

 // 1. User logs in
 "user1" → createRefreshToken() → Token-A (Family: A)

 // 2. After 15 min, user refreshes
 Token-A → verifyAndRotate() → Token-B (Family: A)
 Token-A is now marked "used" ✓

 // 3. Hacker steals Token-A and tries to use it
 Token-A → verifyAndRotate() → ERROR! "Already used!"
 System calls: invalidateTokenFamily("A")
 Token-B also dies! User must re-login.
 */
@Service
public class RefreshTokenService {

    //This service manages refresh tokens - it creates them, validates them,
    //  and makes sure they're not being misused.

    // In-memory usage - In production should use database or redis
    //  - Key: The token string (UUID like "a4b5c6d7-e8f9...")
    //  - Value: RefreshToken object with all details
    //  - ConcurrentHashMap: Thread-safe (multiple users can refresh simultaneously)
    private final Map<String, RefreshToken> refreshTokens = new ConcurrentHashMap<>();

    /** Creates a new refresh token
     - Generates random UUID (like "550e8400-e29b-41d4...")
     - Sets expiry to 7 days from now
     - Stores in the map
     - Returns the token object
     */
    public RefreshToken createRefreshToken(String username) {
        // cleanUp expired tokens periodically
        cleanupExpiredTokens();

        String token = UUID.randomUUID().toString();
        Instant expiredAt = Instant.now().plusSeconds(7 * 24 * 60 * 60); // 7 days
        RefreshToken refreshToken = new RefreshToken(token, username, expiredAt);

        refreshTokens.put(token, refreshToken);
        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return Optional.ofNullable(refreshTokens.get(token));
    }

    public RefreshToken verifyAndRotateToken(String token) {
        RefreshToken oldToken = refreshTokens.get(token);

        if (oldToken == null) {
            throw new RuntimeException("Refresh token not found");
        }

        if (oldToken.isUsed()) {
            // TOKEN REUSE DETECTED! Possible theft!
            invalidateTokenFamily(oldToken.getTokenFamily());
            throw new RuntimeException("Token already used - possible theft detected!");
        }

        if (oldToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        oldToken.setUsed(true);
        //Create New refresh tokem
        RefreshToken refreshToken = createRefreshToken(oldToken.getUsername());
        refreshToken.setTokenFamily(oldToken.getTokenFamily());
        return refreshToken;
    }

    /** What is Token Family - Think of it like a credit card number that stays the same even when you get replacement cards:
     Login → Token1 (Family: ABC123)
     ↓
     Refresh → Token2 (Family: ABC123) - Same family!
     ↓
     Refresh → Token3 (Family: ABC123) - Still same family!
     Scenario: Token2 gets stolen
     Hacker uses Token2 → Gets Token3
     User tries Token2 → ALREADY USED!
     System: "Kill ALL tokens in family ABC123!"
     Result: Token3 also dies, hacker locked out
     */
    private void invalidateTokenFamily(String tokenFamily) {
        refreshTokens.values().stream().filter(token -> token.getTokenFamily().equals(tokenFamily))
                .forEach(t -> t.setUsed(true));
    }

    private void cleanupExpiredTokens() {
        refreshTokens.entrySet().removeIf(entry ->
                entry.getValue().getExpiresAt().isBefore(Instant.now())
        );
    }

}
