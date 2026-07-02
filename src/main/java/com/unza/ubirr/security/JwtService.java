package com.unza.ubirr.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey secretKey;
  private final Duration expiration;

  public JwtService(
    @Value("${app.jwt.secret}") String secret,
    @Value("${app.jwt.expiration:PT12H}") Duration expiration
  ) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiration = expiration;
  }

  public String generateToken(UserPrincipal principal) {
    Instant now = Instant.now();
    var builder = Jwts.builder()
      .id(UUID.randomUUID().toString())
      .subject(principal.id().toString())
      .claim("email", principal.getUsername())
      .claim("roles", principal.roles().stream().map(role -> role.apiValue()).toList())
      .issuedAt(Date.from(now))
      .expiration(Date.from(now.plus(expiration)))
      .signWith(secretKey);
    if (principal.sessionId() != null) {
      builder.claim("sid", principal.sessionId());
    }
    return builder.compact();
  }

  public UUID extractUserId(String token) {
    return UUID.fromString(claims(token).getSubject());
  }

  public String extractJti(String token) {
    return claims(token).getId();
  }

  public String extractSessionId(String token) {
    return claims(token).get("sid", String.class);
  }

  public Duration getExpiration() {
    return expiration;
  }

  public boolean isTokenValid(String token) {
    try {
      claims(token);
      return true;
    } catch (Exception exception) {
      return false;
    }
  }

  private Claims claims(String token) {
    return Jwts.parser()
      .verifyWith(secretKey)
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }
}
