package com.example.demo.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyFactory;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import org.springframework.core.io.Resource;

@Configuration
public class JwtBeans {

    @Value("${app.jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${app.jwt.public-key-path}")
    private Resource publicKeyResource;

    @Bean
    JwtEncoder jwtEncoder() throws Exception {
        RSAPrivateKey privateKey = loadPrivateKey();
        RSAPublicKey publicKey = loadPublicKey();

        var jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("demo-key-2025")
                .build();

        JWKSource<SecurityContext> jwkSource = (jwkSelector, context) -> List.of(jwk);
        return new NimbusJwtEncoder(jwkSource);
    }

    //Visual Flow Diagram
    //
    //  PEM File → Read as String → Remove Headers → Base64 Decode → Binary Data → Java RSA Key Object
    // PKCS8 is standard format for private keys while X509 is standard format for public keys

    private RSAPrivateKey loadPrivateKey() throws Exception {
        // Step 1: Read the key file and convert into to text string
        String key = new String(privateKeyResource.getInputStream().readAllBytes());

        // Remove PEM headers and whitespace
        key = key.replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // Step 3: Decode from Base64
        byte[] keyBytes = Base64.getDecoder().decode(key);

        // Step 4: Create RSA private key object
        // keyFactory is the javatool for creating key objects
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes); // tells java that these bytes are in PKCS8 format
        return (RSAPrivateKey) keyFactory.generatePrivate(spec);
    }

    private RSAPublicKey loadPublicKey() throws Exception {
        String key = new String(publicKeyResource.getInputStream().readAllBytes());

        // Remove PEM headers and whitespace
        key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return (RSAPublicKey) keyFactory.generatePublic(spec);
    }

    @Bean
    JwtDecoder jwtDecoder(CustomJwtValidator customValidator, AudienceValidator audienceValidator) throws Exception {
        RSAPublicKey publicKey = loadPublicKey();

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        // Add custom validator for blacklist checking
        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
                defaultValidators,
                customValidator,
                audienceValidator
        );

        decoder.setJwtValidator(combinedValidator);

        return decoder;
    }

/* Encoder and decoders required for HS256 Alogorithm */

//    @Bean
//    JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String secret) {
//        var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//        var jwk = new OctetSequenceKey.Builder(key)
//                .algorithm(JWSAlgorithm.HS256)
//                .build();
//        JWKSource<SecurityContext> jwkSource = (jwkSelector, context) -> List.of(jwk);
//        return new NimbusJwtEncoder(jwkSource);
//    }
//
//    @Bean
//    JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret) {
//        var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//        return NimbusJwtDecoder.withSecretKey(key)
//                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
//                .build();
//    }
}
