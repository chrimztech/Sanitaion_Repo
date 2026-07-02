package com.unza.ubirr.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class CaptchaService {
  private final boolean enabled;
  private final String provider;
  private final String siteKey;
  private final String secretKey;
  private final RestClient restClient = RestClient.create();

  public CaptchaService(
    @Value("${app.captcha.enabled:false}") boolean enabled,
    @Value("${app.captcha.provider:hcaptcha}") String provider,
    @Value("${app.captcha.site-key:}") String siteKey,
    @Value("${app.captcha.secret-key:}") String secretKey
  ) {
    this.enabled = enabled;
    this.provider = provider;
    this.siteKey = siteKey;
    this.secretKey = secretKey;
  }

  public void verify(String token) {
    if (!enabled) return;
    if (token == null || token.isBlank()) {
      throw new ResponseStatusException(BAD_REQUEST, "Please complete the CAPTCHA verification.");
    }

    String url = "recaptcha".equals(provider)
      ? "https://www.google.com/recaptcha/api/siteverify"
      : "https://hcaptcha.com/siteverify";

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("secret", secretKey);
    form.add("response", token);

    VerifyResponse response = restClient.post()
      .uri(url)
      .contentType(MediaType.APPLICATION_FORM_URLENCODED)
      .body(form)
      .retrieve()
      .body(VerifyResponse.class);

    if (response == null || !response.success()) {
      throw new ResponseStatusException(BAD_REQUEST, "CAPTCHA verification failed. Please try again.");
    }
  }

  public ConfigDto config() {
    return new ConfigDto(enabled, provider, enabled ? siteKey : null);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record VerifyResponse(boolean success) {}

  public record ConfigDto(boolean enabled, String provider, String siteKey) {}
}
