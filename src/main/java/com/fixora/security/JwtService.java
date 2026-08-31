package com.fixora.security;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String generateToken(String username) {
        return jwtTokenProvider.generateToken(username);
    }

    public String extractUsername(String token) {
        return jwtTokenProvider.getUsernameFromJWT(token);
    }

    public boolean isTokenValid(String token, String username) {
        return jwtTokenProvider.validateToken(token) && 
               extractUsername(token).equalsIgnoreCase(username);
    }
}
