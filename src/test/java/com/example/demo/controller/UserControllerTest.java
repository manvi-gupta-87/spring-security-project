package com.example.demo.controller;

import com.example.demo.dtos.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(UserControllerTest.TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserDetailsManager userDetailsManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @EnableWebSecurity
    static class TestSecurityConfig {
        
        @org.springframework.context.annotation.Bean
        public org.springframework.security.web.SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/users/register").permitAll()
                            .requestMatchers("/api/users/me").authenticated()
                            .anyRequest().authenticated()
                    )
                    .csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                    .httpBasic(org.springframework.security.config.Customizer.withDefaults());
            return http.build();
        }
    }

    @Test
    void registerUser_success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUserName("newuser");
        request.setPassword("password123");

        when(userDetailsManager.userExists("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // When & Then
        mvc.perform(post("/api/users/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User created"));
    }

    @Test
    void registerUser_duplicateUsername() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUserName("existinguser");
        request.setPassword("password123");

        when(userDetailsManager.userExists("existinguser")).thenReturn(true);

        // When & Then
        mvc.perform(post("/api/users/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("username already exists"));
    }

    @Test
    void registerUser_missingUsername() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");

        // When & Then
        mvc.perform(post("/api/users/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.details.userName").value("UserName cant be blank"));
    }

    @Test
    void registerUser_missingPassword() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUserName("newuser");

        // When & Then
        mvc.perform(post("/api/users/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.details.password").value("password can not be empty"));
    }

    @Test
    void getCurrentUser_requiresAuthentication() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getCurrentUser_withAuthentication() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testuser"))
                .andExpect(jsonPath("$.authorities").exists());
    }

    @Test
    void me_requires_authentication_returns_401() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"Realm\""));
    }

    @Test
    void register_validation_errors_are_json() throws Exception {
        var bad = Map.of("username","ab", "password","short");
        mvc.perform(post("/api/users/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.userName").exists())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void register_success() throws Exception {
        var ok = Map.of("userName","user", "password","password123");
        
        when(userDetailsManager.userExists("user")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        
        mvc.perform(post("/api/users/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ok)))
                .andExpect(status().isOk())
                .andExpect(content().string("User created"));
    }
}
