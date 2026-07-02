package com.unza.ubirr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AuthDtos {
  private AuthDtos() {
  }

  public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    String email,
    @NotBlank(message = "Password is required")
    String password
  ) {
  }

  public record RegisterRequest(
    @NotBlank(message = "Full name is required")
    String fullName,
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    String email,
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
             message = "Password must contain at least one letter and one number")
    String password,
    String phone,
    String affiliation,
    String unzaId,
    String institution,
    String jobTitle,
    String captchaToken,
    String website   // honeypot — must be blank; bots fill it, humans don't
  ) {
  }

  public record LdapLoginRequest(
    @NotBlank(message = "UNZA ID is required")
    String unzaId,
    @NotBlank(message = "Password is required")
    String password
  ) {
  }

  public record StudentLoginRequest(
    @NotBlank(message = "Student number is required")
    String username,
    @NotBlank(message = "Password is required")
    String password,
    @NotBlank(message = "Programme type is required")
    String instance
  ) {
  }

  public record StaffLoginRequest(
    @NotBlank(message = "Username is required")
    String username,
    @NotBlank(message = "Password is required")
    String password
  ) {
  }

  public record ForgotPasswordRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    String email
  ) {
  }

  public record ForgotPasswordResponse(
    String message
  ) {
  }

  public record ResetPasswordRequest(
    @NotBlank(message = "Reset token is required")
    String token,
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
             message = "Password must contain at least one letter and one number")
    String password
  ) {
  }

  public record ChangePasswordRequest(
    @NotBlank(message = "Current password is required")
    String currentPassword,
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
             message = "Password must contain at least one letter and one number")
    String password
  ) {
  }

  public record CurrentUserDto(
    UUID id,
    String email,
    String fullName,
    String phone,
    String unzaId,
    String authType,
    List<String> roles,
    boolean admin,
    boolean canManageContent,
    Instant lastLoginAt
  ) {
  }

  public record AuthResponse(
    String token,
    CurrentUserDto user
  ) {
  }
}
