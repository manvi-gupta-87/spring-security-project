package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HelloController.class)
@Import(HelloControllerTest.TestSecurityConfig.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mvc;

    @EnableWebSecurity
    static class TestSecurityConfig {
        
        @org.springframework.context.annotation.Bean
        public org.springframework.security.web.SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/hello", "/health").permitAll()
                            .anyRequest().authenticated()
                    )
                    .csrf(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable)
                    .httpBasic(org.springframework.security.config.Customizer.withDefaults());
            return http.build();
        }
    }

    @Test
    void hello_is_public() throws Exception {
        mvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello User - Welcome to our application!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void health_is_public() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Application is running!"));
    }
}