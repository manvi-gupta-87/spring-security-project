package com.example.demo.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// This creates a rate limiter that:
//  - Allows 100 requests per minute per IP address
//  - Returns 429 (Too Many Requests) when limit exceeded
//  - Tracks each IP address separately

// OncePerRequestFilter ensures filter runs only once per request

// 1. Runs BEFORE authentication - Even unauthenticated requests are rate-limited
//  2. Per-IP tracking - Each IP address has its own limit
//  3. Memory storage - Limits are lost on server restart
//  4. No database needed - Everything in ConcurrentHashMap

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Store buckets per IP address. Accepts 100 request from each IP.
    // Bucket refills to 100 tokens every minute
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIP(request);
        Bucket bucket = cache.computeIfAbsent(ip, this::newBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            // Too many requests
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
            response.setContentType("application/json");
        }
    }

    private Bucket newBucket(String ip) {
        // 100 requests per minute per IP
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
