package com.unza.ubirr.service;

import com.unza.ubirr.dto.AuthDtos;
import com.unza.ubirr.model.AppRole;
import com.unza.ubirr.model.AppUser;
import com.unza.ubirr.model.AuthType;
import com.unza.ubirr.model.Notification;
import com.unza.ubirr.model.UserRoleAssignment;
import com.unza.ubirr.model.UserSession;
import com.unza.ubirr.repository.AppUserRepository;
import com.unza.ubirr.repository.NotificationRepository;
import com.unza.ubirr.repository.UserRoleRepository;
import com.unza.ubirr.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.unza.ubirr.security.JwtService;
import com.unza.ubirr.security.UserPrincipal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@Transactional
public class AuthService {
  private final AppUserRepository appUserRepository;
  private final UserRoleRepository userRoleRepository;
  private final NotificationRepository notificationRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthContextService authContextService;
  private final EmailService emailService;
  private final CaptchaService captchaService;
  private final TokenRevocationService tokenRevocationService;
  private final UserSessionRepository userSessionRepository;
  private final int maxAttempts;
  private final int lockoutMinutes;

  public AuthService(
    AppUserRepository appUserRepository,
    UserRoleRepository userRoleRepository,
    NotificationRepository notificationRepository,
    PasswordEncoder passwordEncoder,
    JwtService jwtService,
    AuthContextService authContextService,
    EmailService emailService,
    CaptchaService captchaService,
    TokenRevocationService tokenRevocationService,
    UserSessionRepository userSessionRepository,
    @Value("${app.security.lockout.max-attempts:5}") int maxAttempts,
    @Value("${app.security.lockout.duration-minutes:15}") int lockoutMinutes
  ) {
    this.appUserRepository = appUserRepository;
    this.userRoleRepository = userRoleRepository;
    this.notificationRepository = notificationRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authContextService = authContextService;
    this.emailService = emailService;
    this.captchaService = captchaService;
    this.tokenRevocationService = tokenRevocationService;
    this.userSessionRepository = userSessionRepository;
    this.maxAttempts = maxAttempts;
    this.lockoutMinutes = lockoutMinutes;
  }

  public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
    // Honeypot: bots fill hidden fields, humans leave them blank
    if (request.website() != null && !request.website().isBlank()) {
      return new AuthDtos.AuthResponse(null, null);
    }

    captchaService.verify(request.captchaToken());

    if (appUserRepository.existsByEmailIgnoreCase(request.email())) {
      throw new ResponseStatusException(BAD_REQUEST, "An account with that email already exists");
    }

    AppUser user = new AppUser();
    user.email = request.email().trim().toLowerCase();
    user.passwordHash = passwordEncoder.encode(request.password());
    user.fullName = request.fullName().trim();
    user.status = "pending";
    user.emailVerified = false;
    user.emailVerificationToken = UUID.randomUUID().toString();
    if (request.phone() != null && !request.phone().isBlank()) user.phone = request.phone().trim();
    if (request.affiliation() != null && !request.affiliation().isBlank()) user.affiliation = request.affiliation().trim();
    if (request.institution() != null && !request.institution().isBlank()) user.institution = request.institution().trim();
    if (request.jobTitle() != null && !request.jobTitle().isBlank()) user.jobTitle = request.jobTitle().trim();
    if (request.unzaId() != null && !request.unzaId().isBlank()) {
      String unzaId = request.unzaId().trim();
      if (!appUserRepository.existsByUnzaId(unzaId)) user.unzaId = unzaId;
    }
    user = appUserRepository.save(user);

    AppRole initialRole = "unza_staff".equalsIgnoreCase(request.affiliation()) ? AppRole.STAFF : AppRole.STUDENT;
    UserRoleAssignment roleAssignment = new UserRoleAssignment();
    roleAssignment.user = user;
    roleAssignment.role = initialRole;
    userRoleRepository.save(roleAssignment);

    emailService.sendVerificationEmail(user.email, user.emailVerificationToken);

    Notification notification = new Notification();
    notification.user = user;
    notification.title = "Welcome to Sanitation Hub";
    notification.message = "Your account has been created. Please verify your email address to activate it.";
    notificationRepository.save(notification);

    return new AuthDtos.AuthResponse(null, null);
  }

  public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
    AppUser user = appUserRepository.findByEmailIgnoreCase(request.email().trim())
      .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid email or password"));

    if (isLockedOut(user)) {
      throw new ResponseStatusException(
        TOO_MANY_REQUESTS,
        "Account temporarily locked due to too many failed attempts. Try again later."
      );
    }

    if (!passwordEncoder.matches(request.password(), user.passwordHash)) {
      recordFailedAttempt(user);
      throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password");
    }

    if (!user.emailVerified) {
      throw new ResponseStatusException(
        UNAUTHORIZED,
        "Please verify your email address before signing in. Check your inbox for a verification link."
      );
    }

    if ("suspended".equals(user.status)) {
      throw new ResponseStatusException(UNAUTHORIZED, "Your account has been suspended. Contact support.");
    }

    user.loginAttempts = 0;
    user.lockedUntil = null;
    user.lastLoginAt = Instant.now();
    appUserRepository.save(user);
    return authResponse(user);
  }

  public void verifyEmail(String token) {
    AppUser user = appUserRepository.findByEmailVerificationToken(token)
      .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Invalid or expired verification link"));
    user.emailVerified = true;
    user.status = "active";
    user.emailVerificationToken = null;
    appUserRepository.save(user);
  }

  public AuthDtos.CurrentUserDto me() {
    return currentUserDto(authContextService.requireUser());
  }

  public AuthDtos.ForgotPasswordResponse forgotPassword(AuthDtos.ForgotPasswordRequest request) {
    // Always return success to prevent email enumeration
    appUserRepository.findByEmailIgnoreCase(request.email().trim()).ifPresent(user -> {
      String rawToken = UUID.randomUUID().toString();
      user.resetToken = sha256(rawToken);
      user.resetTokenExpiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
      appUserRepository.save(user);
      emailService.sendPasswordResetEmail(user.email, rawToken);
    });
    return new AuthDtos.ForgotPasswordResponse(
      "If an account with that email exists, a reset link has been sent."
    );
  }

  public void resetPassword(AuthDtos.ResetPasswordRequest request) {
    String tokenHash = sha256(request.token());
    AppUser user = appUserRepository.findByResetToken(tokenHash)
      .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Invalid or expired reset link"));
    if (user.resetTokenExpiresAt == null || user.resetTokenExpiresAt.isBefore(Instant.now())) {
      throw new ResponseStatusException(BAD_REQUEST, "Reset link has expired. Please request a new one.");
    }
    user.passwordHash = passwordEncoder.encode(request.password());
    user.resetToken = null;
    user.resetTokenExpiresAt = null;
    user.loginAttempts = 0;
    user.lockedUntil = null;
    appUserRepository.save(user);
  }

  public void changePassword(AuthDtos.ChangePasswordRequest request) {
    AppUser user = authContextService.requireUser();
    if (user.authType != null && (user.authType == AuthType.LDAP
        || user.authType == AuthType.OAP
        || user.authType == AuthType.HR)) {
      throw new ResponseStatusException(BAD_REQUEST,
        "Your password is managed by UNZA. Please contact the UNZA ICT helpdesk.");
    }
    if (!passwordEncoder.matches(request.currentPassword(), user.passwordHash)) {
      throw new ResponseStatusException(UNAUTHORIZED, "Current password is incorrect");
    }
    user.passwordHash = passwordEncoder.encode(request.password());
    appUserRepository.save(user);
  }

  public AuthDtos.AuthResponse authResponse(AppUser user) {
    // Rotate session ID — any token from a previous login is now invalid
    AppUser refreshed = appUserRepository.findByIdWithRoles(user.id).orElseThrow();
    String newSessionId = UUID.randomUUID().toString();
    // Revoke any previous active session record
    userSessionRepository.findByUser_IdOrderByCreatedAtDesc(refreshed.id).stream()
      .filter(s -> s.revokedAt == null)
      .forEach(s -> { s.revokedAt = Instant.now(); userSessionRepository.save(s); });
    refreshed.currentSessionId = newSessionId;
    appUserRepository.save(refreshed);
    // Record the new session
    UserSession session = new UserSession();
    session.sessionId = newSessionId;
    session.user = refreshed;
    session.ipAddress = currentIp();
    session.userAgent = currentUserAgent();
    session.expiresAt = Instant.now().plus(jwtService.getExpiration());
    userSessionRepository.save(session);
    UserPrincipal principal = UserPrincipal.from(refreshed);
    return new AuthDtos.AuthResponse(jwtService.generateToken(principal), currentUserDto(refreshed));
  }

  public void logout(String bearerToken) {
    if (bearerToken == null || !bearerToken.startsWith("Bearer ")) return;
    String token = bearerToken.substring(7);
    if (!jwtService.isTokenValid(token)) return;
    // Revoke the JTI in Redis so the token is immediately dead
    String jti = jwtService.extractJti(token);
    if (jti != null) tokenRevocationService.revoke(jti, jwtService.getExpiration());
    // Clear the server-side session and mark the session record as revoked
    UUID userId = jwtService.extractUserId(token);
    if (userId != null) {
      String sid = jwtService.extractSessionId(token);
      appUserRepository.findById(userId).ifPresent(u -> {
        u.currentSessionId = null;
        appUserRepository.save(u);
      });
      if (sid != null) {
        userSessionRepository.findBySessionId(sid).ifPresent(s -> {
          s.revokedAt = Instant.now();
          userSessionRepository.save(s);
        });
      }
    }
  }

  private String currentIp() {
    try {
      HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
      String forwarded = req.getHeader("X-Forwarded-For");
      return forwarded != null ? forwarded.split(",")[0].trim() : req.getRemoteAddr();
    } catch (Exception e) {
      return null;
    }
  }

  private String currentUserAgent() {
    try {
      String ua = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
        .getRequest().getHeader("User-Agent");
      return ua != null && ua.length() > 500 ? ua.substring(0, 497) + "…" : ua;
    } catch (Exception e) {
      return null;
    }
  }

  public AuthDtos.CurrentUserDto currentUserDto(AppUser user) {
    AppUser hydrated = appUserRepository.findById(user.id).orElseThrow();
    List<String> roles = hydrated.roles.stream().map(role -> role.role.apiValue()).toList();
    return new AuthDtos.CurrentUserDto(
      hydrated.id,
      hydrated.email,
      hydrated.fullName,
      hydrated.phone,
      hydrated.unzaId,
      hydrated.authType != null ? hydrated.authType.name().toLowerCase() : "local",
      roles,
      authContextService.isAdmin(hydrated),
      authContextService.canManageContent(hydrated),
      hydrated.lastLoginAt
    );
  }

  private boolean isLockedOut(AppUser user) {
    return user.lockedUntil != null && user.lockedUntil.isAfter(Instant.now());
  }

  private void recordFailedAttempt(AppUser user) {
    user.loginAttempts = user.loginAttempts + 1;
    if (user.loginAttempts >= maxAttempts) {
      user.lockedUntil = Instant.now().plus(lockoutMinutes, ChronoUnit.MINUTES);
    }
    appUserRepository.save(user);
  }

  private String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
