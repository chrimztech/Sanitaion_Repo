package com.unza.ubirr.controller;

import com.unza.ubirr.dto.AuthDtos;
import com.unza.ubirr.service.AuthService;
import com.unza.ubirr.service.HrAuthService;
import com.unza.ubirr.service.LdapAuthService;
import com.unza.ubirr.service.OapAuthService;
import com.unza.ubirr.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final LdapAuthService ldapAuthService;
  private final OapAuthService oapAuthService;
  private final HrAuthService hrAuthService;
  private final RateLimitService rateLimitService;

  public AuthController(
    AuthService authService,
    LdapAuthService ldapAuthService,
    OapAuthService oapAuthService,
    HrAuthService hrAuthService,
    RateLimitService rateLimitService
  ) {
    this.authService = authService;
    this.ldapAuthService = ldapAuthService;
    this.oapAuthService = oapAuthService;
    this.hrAuthService = hrAuthService;
    this.rateLimitService = rateLimitService;
  }

  @PostMapping("/register")
  public AuthDtos.AuthResponse register(
    @Valid @RequestBody AuthDtos.RegisterRequest request,
    HttpServletRequest httpServletRequest
  ) {
    rateLimitService.checkRegister(getClientIp(httpServletRequest));
    return authService.register(request);
  }

  @PostMapping("/login")
  public AuthDtos.AuthResponse login(
    @Valid @RequestBody AuthDtos.LoginRequest request,
    HttpServletRequest httpServletRequest
  ) {
    rateLimitService.checkLogin(getClientIp(httpServletRequest));
    return authService.login(request);
  }

  @GetMapping("/me")
  public AuthDtos.CurrentUserDto me() {
    return authService.me();
  }

  @GetMapping("/verify-email")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void verifyEmail(@RequestParam String token) {
    authService.verifyEmail(token);
  }

  @PostMapping("/forgot-password")
  public AuthDtos.ForgotPasswordResponse forgotPassword(
    @Valid @RequestBody AuthDtos.ForgotPasswordRequest request,
    HttpServletRequest httpServletRequest
  ) {
    rateLimitService.checkPasswordReset(getClientIp(httpServletRequest));
    return authService.forgotPassword(request);
  }

  @PostMapping("/reset-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request, HttpServletRequest httpServletRequest) {
    rateLimitService.checkPasswordReset(getClientIp(httpServletRequest));
    authService.resetPassword(request);
  }

  @PostMapping("/change-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(@Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
    authService.changePassword(request);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(HttpServletRequest request) {
    authService.logout(request.getHeader("Authorization"));
  }

  @PostMapping("/ldap-login")
  public AuthDtos.AuthResponse ldapLogin(@Valid @RequestBody AuthDtos.LdapLoginRequest request, HttpServletRequest httpServletRequest) {
    rateLimitService.checkLogin(getClientIp(httpServletRequest));
    return ldapAuthService.ldapLogin(request);
  }

  @PostMapping("/student-login")
  public AuthDtos.AuthResponse studentLogin(@Valid @RequestBody AuthDtos.StudentLoginRequest request, HttpServletRequest httpServletRequest) {
    rateLimitService.checkLogin(getClientIp(httpServletRequest));
    return oapAuthService.studentLogin(request);
  }

  @PostMapping("/staff-login")
  public AuthDtos.AuthResponse staffLogin(@Valid @RequestBody AuthDtos.StaffLoginRequest request, HttpServletRequest httpServletRequest) {
    rateLimitService.checkLogin(getClientIp(httpServletRequest));
    return hrAuthService.staffLogin(request);
  }

  private String getClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
