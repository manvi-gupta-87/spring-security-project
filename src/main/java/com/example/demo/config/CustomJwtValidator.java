package com.example.demo.config;

import com.example.demo.service.TokenBlacklistService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
public class CustomJwtValidator implements OAuth2TokenValidator<Jwt> {

    private final TokenBlacklistService tokenBlacklistService;

    public CustomJwtValidator(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String jti = token.getClaimAsString("jti");
        if (tokenBlacklistService.isBlackListed(jti)){
            OAuth2Error error = new OAuth2Error(
                    "token is blacklisted",
                    "This token has been revoked",
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        }
        return OAuth2TokenValidatorResult.success();
    }
}
