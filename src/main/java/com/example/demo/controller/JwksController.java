package com.example.demo.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/**  - The JWKS endpoint is publicly accessible (no authentication required)
 - Only the public key is exposed, never the private key
 - The key ID (kid) helps identify which key was used to sign a token
 - This follows the standard JWKS format used by OAuth2/OIDC
 - External services can now fetch your public key to verify tokens
 */
@RestController
public class JwksController {

    @Value("${app.jwt.public-key-path}")
    private Resource publicKeyResource;


    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() throws Exception {
        RSAPublicKey rsaPublicKey = loadPublicKey();

        //Create JWK with keyID
        RSAKey jwk = new RSAKey.Builder(rsaPublicKey)
                .keyUse(KeyUse.SIGNATURE)
                .keyID("demo-key-2025")  // Unique identifier for this key
                .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                .build();

        // Create JWK Set
        JWKSet jwkSet = new JWKSet(jwk);

        // Return as Map for Spring to serialize
        return jwkSet.toJSONObject();
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

}
