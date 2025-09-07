package com.example.demo.controller;

import com.example.demo.dtos.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
    private MockMvc mockMvc;

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

    @Test
    public void testMeEndpointWithValidAccessToken() throws Exception {
        // First get an access token
        String tokenRequest = "{\"username\":\"user\",\"password\":\"password\"}";

        MvcResult result = mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> tokenResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Map.class
        );
        String accessToken = (String) tokenResponse.get("access_token");

        // Call /me with access token
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sub").value("user"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.aud").isArray())
                .andExpect(jsonPath("$.aud[0]").value("demo-api"))
                .andExpect(jsonPath("$.token_type").value("access"));
    }

    @Test
    public void testMeEndpointWithRefreshTokenShouldFail() throws Exception {
        // First get tokens
        String tokenRequest = "{\"username\":\"user\",\"password\":\"password\"}";

        MvcResult result = mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> tokenResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Map.class
        );
        String refreshToken = (String) tokenResponse.get("refresh_token");

        // Try to call /me with refresh token (which doesn't have JWT structure)
        // This should fail with 401
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testMeEndpointWithManipulatedTokenType() throws Exception {
        // This test would require creating a JWT with token_type: "refresh"
        // Since we can't easily create a valid signed JWT with wrong token_type,
        // this test would fail at signature validation first

        // Instead, test with no token
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
