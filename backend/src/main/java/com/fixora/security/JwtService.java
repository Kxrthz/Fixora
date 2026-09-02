package com.fixora.security;

import com.fixora.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${fixora.jwt-secret}")
    private String jwtSecret;

    @Value("${fixora.jwt-expiration-ms:86400000}")
    private long jwtExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String issue(User user, long seconds) {
        Date now = new Date();

        return Jwts.builder()
                .subject(user.email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + seconds * 1000))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public String extractUsername(String token){
        return parse(token).getPayload().getSubject();
    }
}
