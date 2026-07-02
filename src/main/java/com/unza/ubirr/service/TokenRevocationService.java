package com.unza.ubirr.service;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenRevocationService {
  private static final Logger log = LoggerFactory.getLogger(TokenRevocationService.class);
  private static final String PREFIX = "revoked:jti:";
  private final StringRedisTemplate redis;

  public TokenRevocationService(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public void revoke(String jti, Duration ttl) {
    try {
      redis.opsForValue().set(PREFIX + jti, "1", ttl);
    } catch (Exception e) {
      log.warn("Token revocation skipped — Redis unavailable: {}", e.getMessage());
    }
  }

  public boolean isRevoked(String jti) {
    try {
      return Boolean.TRUE.equals(redis.hasKey(PREFIX + jti));
    } catch (Exception e) {
      log.warn("Token revocation check skipped — Redis unavailable: {}", e.getMessage());
      return false;
    }
  }
}
