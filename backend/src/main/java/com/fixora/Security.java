package com.fixora;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration @EnableWebSecurity
class SecurityConfiguration {
  @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
  @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable).cors(c -> {}).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a -> a.requestMatchers("/api/v1/auth/**", "/actuator/health").permitAll().requestMatchers(HttpMethod.GET, "/api/v1/services/**", "/api/v1/providers/**").permitAll().anyRequest().authenticated())
      .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
  }
  @Bean CorsConfigurationSource corsConfigurationSource(@Value("${fixora.cors-origins}") String origins) {
    var config=new CorsConfiguration(); config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList()); config.setAllowedMethods(List.of("GET","POST","PATCH","PUT","DELETE","OPTIONS")); config.setAllowedHeaders(List.of("Authorization","Content-Type")); config.setAllowCredentials(true);
    var source=new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",config); return source;
  }
}

@org.springframework.stereotype.Service
class JwtService {
  private final SecretKey key;
  JwtService(@Value("${fixora.jwt-secret}") String secret) { key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
  String issue(User user, long seconds) { return Jwts.builder().subject(user.email).claim("role",user.role.name()).issuedAt(Date.from(Instant.now())).expiration(Date.from(Instant.now().plusSeconds(seconds))).signWith(key).compact(); }
  Jws<Claims> parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token); }
}

@org.springframework.stereotype.Component
class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwt; JwtFilter(JwtService jwt){this.jwt=jwt;}
  @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
    String header=request.getHeader("Authorization");
    if(header!=null&&header.startsWith("Bearer ")) try { var claims=jwt.parse(header.substring(7)).getPayload(); var auth=new UsernamePasswordAuthenticationToken(claims.getSubject(),null,List.of(new SimpleGrantedAuthority("ROLE_"+claims.get("role",String.class)))); org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth); } catch(JwtException ignored) {}
    chain.doFilter(request,response);
  }
}
