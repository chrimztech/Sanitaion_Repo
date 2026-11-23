package com.unza.ubirr.service;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RateLimitService {
  private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
  private static final String PREFIX = "rate:auth:";
  private final StringRedisTemplate redis;
  private final int loginMax;
  private final int registerMax;
  private final long windowSeconds;

  public RateLimitService(
    StringRedisTemplate redis,
    @Value("${app.security.rate-limit.login-max:10}") int loginMax,
    @Value("${app.security.rate-limit.register-max:5}") int registerMax,
    @Value("${app.security.rate-limit.window-seconds:60}") long windowSeconds
  ) {
    this.redis = redis;
    this.loginMax = loginMax;
    this.registerMax = registerMax;
    this.windowSeconds = windowSeconds;
  }

  public void checkLogin(String ip) {
    check("login:" + sanitize(ip), loginMax);
  }

  public void checkRegister(String ip) {
    check("register:" + sanitize(ip), registerMax);
  }

  // Password reset requests and the alternate (LDAP/student/staff) login paths are just as
  // brute-forceable as the main login endpoint but previously had no limit at all — reuse the
  // login bucket's threshold for all of them.
  public void checkPasswordReset(String ip) {
    check("pwreset:" + sanitize(ip), loginMax);
  }

  private void check(String key, int max) {
    String fullKey = PREFIX + key;
    try {
      Long count = redis.opsForValue().increment(fullKey);
      if (count != null && count == 1) {
        redis.expire(fullKey, Duration.ofSeconds(windowSeconds));
      }
      if (count != null && count > max) {
        throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS,
          "Too many requests. Please try again in a moment."
        );
      }
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      log.warn("Rate limit check skipped — Redis unavailable: {}", e.getMessage());
    }
  }

  private String sanitize(String ip) {
    return ip == null ? "unknown" : ip.replaceAll("[^a-zA-Z0-9.:\\-]", "_");
  }
}
