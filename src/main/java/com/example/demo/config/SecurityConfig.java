package com.example.demo.config;

import com.example.demo.errors.JsonAccessDeniedHandler;
import com.example.demo.errors.JsonAuthEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security Configuration
 * 
 * Filter Chain Architecture (in order of execution):
 * 1. SecurityContextPersistenceFilter - Establishes SecurityContext from session (disabled in stateless)
 * 2. HeaderWriterFilter - Adds security headers to response
 * 3. CorsFilter - Handles CORS preflight and headers
 * 4. CsrfFilter - CSRF protection (disabled for REST APIs)
 * 5. LogoutFilter - Processes logout requests
 * 6. OAuth2AuthorizationRequestRedirectFilter - OAuth2 login flow
 * 7. UsernamePasswordAuthenticationFilter - Form login processing
 * 8. DefaultLoginPageGeneratingFilter - Generates default login page
 * 9. BasicAuthenticationFilter - HTTP Basic authentication
 * 10. BearerTokenAuthenticationFilter - JWT token processing (OAuth2 Resource Server)
 * 11. RequestCacheAwareFilter - Saves/restores requests
 * 12. SecurityContextHolderAwareRequestFilter - Servlet API integration
 * 13. AnonymousAuthenticationFilter - Creates anonymous authentication
 * 14. SessionManagementFilter - Session fixation protection
 * 15. ExceptionTranslationFilter - Catches security exceptions, triggers authentication
 * 16. FilterSecurityInterceptor - Final authorization decisions
 * 
 * In this configuration:
 * - Stateless session (no session creation)
 * - JWT Bearer token authentication via OAuth2 Resource Server
 * - Custom JSON error responses for 401/403
 * - Role hierarchy: ADMIN > USER
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Map JWT claim "roles" -> GrantedAuthority (no "SCOPE_" prefix)
    private JwtAuthenticationConverter jwtAuthConverter() {
        var gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName("roles");
        gac.setAuthorityPrefix("");
        var jac = new JwtAuthenticationConverter();
        jac.setJwtGrantedAuthoritiesConverter(gac);
        return jac;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        var h = new RoleHierarchyImpl();
        h.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return h;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JsonAuthEntryPoint entryPoint,
                                           JsonAccessDeniedHandler deniedHandler,
                                           CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                //  - nosniff: Stops browser from guessing file types
                //  - DENY: Prevents your site being embedded in iframes
                //  - XSS protection: Enables browser's built-in XSS filter
                //  - HSTS: Forces HTTPS for 1 year (only works on HTTPS sites)
                //  - CSP: Only allows resources from your own domain
                .headers(headers-> headers
                        .contentTypeOptions(Customizer.withDefaults()) // X-Content-Type-Options: nosniff
                        .frameOptions(frame -> frame.deny()) // // X-Frame-Options: DENY
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))  // X-XSS-Protection: 1; mode=block))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))  // HSTS: max-age=31536000; includeSubDomains
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'"))  // Basic CSP
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/hello", "/health", "/api/token", "/.well-known/jwks.json").permitAll()
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/manager", "/api/manager/").permitAll()
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/security-context").authenticated()
                        .requestMatchers("/api/basic-auth-demo").authenticated()
                        .requestMatchers("/api/token/refresh").permitAll()
                        .requestMatchers("/api/v1/**", "/api/v2/**", "/api/versions").permitAll()
                        .requestMatchers("/api/logout").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)   // 401 JSON
                        .accessDeniedHandler(deniedHandler)     // 403 JSON
                )
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                );
        return http.build();
    }
}