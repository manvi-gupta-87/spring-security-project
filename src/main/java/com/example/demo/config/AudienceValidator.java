package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/** Checks if the token's audience matches with configured value
 * if no match - 401 unauthorized
 * if match - continue processing
 *  This prevents from token issued for service A being used by service B
 * */

@Configuration
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    @Value("${app.jwt.audience}")
    private String expectedAudience;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        List<String> audiences = token.getAudience();
        if (!CollectionUtils.isEmpty(audiences) && audiences.contains(expectedAudience)) {
            return OAuth2TokenValidatorResult.success();
        }
        OAuth2Error error = new OAuth2Error(
                "invalid_audience",
                String.format("The required audience '%s' is not present in the token", expectedAudience),
                null);
        return OAuth2TokenValidatorResult.failure(error);
    }
}
