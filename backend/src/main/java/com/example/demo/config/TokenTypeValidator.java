package com.example.demo.config;

import ch.qos.logback.core.util.StringUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
public class TokenTypeValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error error = new OAuth2Error(
            "invalid_token_type",
            "Token type must be 'access'",
            null
    );

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String token_type = token.getClaimAsString("token_type");

        if (StringUtil.isNullOrEmpty(token_type)|| !"access".equals(token_type)) {
            return OAuth2TokenValidatorResult.failure(error);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
