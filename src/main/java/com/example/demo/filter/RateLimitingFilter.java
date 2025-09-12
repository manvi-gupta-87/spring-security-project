package com.example.demo.filter;

import com.nimbusds.jose.proc.SecurityContext;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    //ADMIN 1000/min, USER 200/min, IP 60/min
    private final Map<String, Bucket> ipCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> userCache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> adminCache = new ConcurrentHashMap<>();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth  = SecurityContextHolder.getContext().getAuthentication();
        Bucket bucket;
        String rateLimitInfo;

        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            boolean isAdmin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority )
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));

            if (isAdmin) {
                //admin per 1000 reqest per minute
                bucket = adminCache.computeIfAbsent(auth.getName(), this::newAdminBucket);
                rateLimitInfo = "ADMIN (1000/min)";
            } else {
                bucket = userCache.computeIfAbsent(auth.getName(),this::newUserBucket);
                rateLimitInfo = "USER (200/min)";
            }
        } else {
            String ip = getClientIP(request);
            bucket = ipCache.computeIfAbsent(ip, this::newIpBucket);
            rateLimitInfo = "IP (60/min)";
        }

        if (bucket.tryConsume(1)) {
            // Add rate limit info header
            response.addHeader("X-Rate-Limit-Type", rateLimitInfo);
            filterChain.doFilter(request, response);
        } else {
            // Too many requests
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"error\": \"Too many requests. Rate limit: %s. Please try again later.\"}",
                    rateLimitInfo));
        }
    }

    private Bucket newIpBucket(String ip) {
        // 100 requests per minute per IP
        Bandwidth limit = Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket newUserBucket(String ip) {
        // 100 requests per minute per IP
        Bandwidth limit = Bandwidth.classic(200, Refill.intervally(200, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket newAdminBucket(String ip) {
        // 100 requests per minute per IP
        Bandwidth limit = Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(1)));
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
