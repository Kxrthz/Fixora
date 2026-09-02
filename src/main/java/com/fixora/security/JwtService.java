package com.fixora.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${fixora.jwt-secret:development-secret-change-this-to-at-least-32-bytes-long-for-security}")
    private String jwtSecret;

    @Value("${fixora.jwt-expiration-ms:86400000}")
    private long jwtExpirationInMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.error("JWT_SECRET must be at least 32 bytes (256 bits) long for HMAC-SHA256 safety!");
            throw new IllegalArgumentException("JWT_SECRET environment variable is too short. Minimum length is 32 characters.");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        return issue(username, jwtExpirationInMs / 1000);
    }

    public String issue(String username, long expirationInSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (expirationInSeconds * 1000));

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            log.error("Invalid or expired JWT token: {}", ex.getMessage());
        }
        return false;
    }

    public boolean isTokenValid(String token, String username) {
        return validate(token) && extractUsername(token).equalsIgnoreCase(username);
    }
}