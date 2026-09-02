package com.unza.ubirr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;
  private final boolean enabled;
  private final String from;
  private final String baseUrl;

  public EmailService(
    JavaMailSender mailSender,
    @Value("${app.mail.enabled:false}") boolean enabled,
    @Value("${app.mail.from}") String from,
    @Value("${app.mail.base-url}") String baseUrl
  ) {
    this.mailSender = mailSender;
    this.enabled = enabled;
    this.from = from;
    this.baseUrl = baseUrl;
  }

  public void sendVerificationEmail(String to, String token) {
    String link = baseUrl + "/verify-email?token=" + token;
    String body = """
      Welcome to the UNZA Sanitation Hub!

      Please verify your email address by clicking the link below:

      %s

      This link expires in 24 hours. If you did not create an account, you can ignore this email.

      — The UNZA Sanitation Hub Team
      """.formatted(link);
    send(to, "Verify your email — UNZA Sanitation Hub", body);
  }

  public void sendPasswordResetEmail(String to, String token) {
    String link = baseUrl + "/reset-password?token=" + token;
    String body = """
      You requested a password reset for your UNZA Sanitation Hub account.

      Click the link below to set a new password:

      %s

      This link expires in 1 hour. If you did not request a reset, you can safely ignore this email.

      — The UNZA Sanitation Hub Team
      """.formatted(link);
    send(to, "Reset your password — UNZA Sanitation Hub", body);
  }

  private void send(String to, String subject, String body) {
    if (!enabled) {
      log.warn("Mail disabled (app.mail.enabled=false) — not sending \"{}\" to {}", subject, to);
      return;
    }
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);
    try {
      mailSender.send(message);
    } catch (Exception e) {
      // Never let an SMTP outage fail the caller — e.g. account registration must still
      // succeed even if the verification email can't be delivered right now.
      log.error("Failed to send \"{}\" to {}: {}", subject, to, e.getMessage());
    }
  }
}
