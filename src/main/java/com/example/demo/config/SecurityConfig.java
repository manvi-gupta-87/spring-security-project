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
                                           JsonAccessDeniedHandler deniedHandler) throws Exception {
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/hello", "/health", "/api/token").permitAll()
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/manager", "/api/manager/").permitAll()
                        .requestMatchers("/api/users/me").authenticated()
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