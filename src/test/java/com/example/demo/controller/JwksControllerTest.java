package com.example.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class JwksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testJwksEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk());
    }

    @Test
    public void testJwksContainsRequiredFields() throws Exception {
        String response = mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].use").value("sig"))
                .andExpect(jsonPath("$.keys[0].kid").value("demo-key-2025"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
                .andExpect(jsonPath("$.keys[0].n").exists())  // RSA modulus
                .andExpect(jsonPath("$.keys[0].e").exists())  // RSA exponent
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify it's valid JSON
        JsonNode json = objectMapper.readTree(response);
        assert json.has("keys");
    }

    @Test
    public void testJwksDoesNotContainPrivateKey() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].d").doesNotExist())  // Private exponent should not exist
                .andExpect(jsonPath("$.keys[0].p").doesNotExist())  // Prime p should not exist
                .andExpect(jsonPath("$.keys[0].q").doesNotExist()); // Prime q should not exist
    }
}