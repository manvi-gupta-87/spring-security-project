package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequest {
    @NotBlank
    public String refresh_token;
}

