package com.quickbite.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Validates HS256 JWTs at the edge. The signing secret ({@code JWT_SECRET}) is shared with
 * identity-service (the issuer) and every downstream service, so the gateway can verify tokens
 * without a network call. Verification failures surface as {@link io.jsonwebtoken.JwtException}.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer:quickbite-identity}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
    }

    /**
     * Parses and verifies the signature, issuer and expiry of the token.
     *
     * @return the validated claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid, expired or tampered with
     */
    public Claims parse(String token) {
        Jws<Claims> jws = Jwts.parser()
                .requireIssuer(issuer)
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        return jws.getPayload();
    }
}
