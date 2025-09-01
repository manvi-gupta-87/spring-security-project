package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.config.Customizer;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ManagerController.class)
@Import(ManagerControllerTest.TestSecurityConfig.class)
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserRole_thenCannotAccessManagerUpdate() throws Exception {
        mockMvc.perform(get("/api/manager/update"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminRole_thenCanAccessManagerUpdate() throws Exception {
        mockMvc.perform(get("/api/manager/update"))
                .andExpect(status().isOk())
                .andExpect(content().string("Data is updated"));
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void whenManagerRole_thenCanAccessManagerUpdate() throws Exception {
        mockMvc.perform(get("/api/manager/update"))
                .andExpect(status().isOk())
                .andExpect(content().string("Data is updated"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserRole_thenCanAccessManagerRoot() throws Exception {
        mockMvc.perform(get("/api/manager"))
                .andExpect(status().isOk())
                .andExpect(content().string("Manager API Root - Available endpoints: /update"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserRole_thenCanAccessManagerRootWithSlash() throws Exception {
        mockMvc.perform(get("/api/manager/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Manager API - Available endpoints: /update"));
    }

    @Test
    void whenNoAuthentication_thenCannotAccessManagerUpdate() throws Exception {
        mockMvc.perform(get("/api/manager/update"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenNoAuthentication_thenCanAccessManagerRoot() throws Exception {
        mockMvc.perform(get("/api/manager"))
                .andExpect(status().isOk())
                .andExpect(content().string("Manager API Root - Available endpoints: /update"));
    }

    @Test
    void whenNoAuthentication_thenCanAccessManagerRootWithSlash() throws Exception {
        mockMvc.perform(get("/api/manager/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Manager API - Available endpoints: /update"));
    }

    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/hello", "/health").permitAll()
                            .requestMatchers("/api/users/register").permitAll()
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/manager", "/api/manager/").permitAll()
                            .requestMatchers("/api/users/me").authenticated()
                            .anyRequest().authenticated()
                    )
                    .csrf(AbstractHttpConfigurer::disable)
                    .httpBasic(Customizer.withDefaults());
            return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsManager userDetailsManager() {
            return new InMemoryUserDetailsManager();
        }
    }
}
