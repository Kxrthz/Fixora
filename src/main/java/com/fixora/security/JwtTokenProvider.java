package com.fixora.security;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtService jwtService;

    public JwtTokenProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public JwtService getJwtService() {
        return jwtService;
    }
}