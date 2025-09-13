package com.example.demo.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Feature: Logout with blacklisting
@Service
public class TokenBlacklistService {

    Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Add a token to the blacklist
     * @param jti The JWT ID to blacklist
     * @param expiresAt When the token naturally expires
     */
    public void blackListToken(String jti, Instant expiresAt) {
        if (jti != null && expiresAt != null) {
            blacklistedTokens.put(jti, expiresAt);
        }
    }

    /**
     * Check if a token is blacklisted
     * @param jti The JWT ID to check
     * @return true if blacklisted, false otherwise
     */
    public boolean isBlackListed(String jti) {
        if (jti == null) {
            return false;
        }
        Instant expiresAt = blacklistedTokens.get(jti);
        if (expiresAt != null) {
            if (expiresAt.isBefore(Instant.now())) {
                blacklistedTokens.remove(jti);
                return false; /// token is expired
            } else {
                return true; // token is blacklisted
            }
        }
        return false;
    }


    /**
     * Clean up expired tokens from blacklist
     * Runs every hour
     */
    @Scheduled(fixedDelay = 3600000) // 1 hour
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry ->
                entry.getValue().isBefore(now)
        );
    }

    /**
     * Get the size of blacklist (for monitoring)
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

}
