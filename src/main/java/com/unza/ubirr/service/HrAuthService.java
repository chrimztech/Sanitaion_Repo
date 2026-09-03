package com.unza.ubirr.service;

import com.unza.ubirr.dto.AuthDtos;
import com.unza.ubirr.model.AppRole;
import com.unza.ubirr.model.AppUser;
import com.unza.ubirr.model.AuthType;
import com.unza.ubirr.model.Notification;
import com.unza.ubirr.model.UserRoleAssignment;
import com.unza.ubirr.repository.AppUserRepository;
import com.unza.ubirr.repository.NotificationRepository;
import com.unza.ubirr.repository.UserRoleRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class HrAuthService {

  private static final String HR_LOGIN_URL = "https://hr.unza.zm/api/auth-login2";
  private static final String HR_METADATA_URL = "https://hr.unza.zm/api/get-meta-data-on-user";

  private final AppUserRepository appUserRepository;
  private final UserRoleRepository userRoleRepository;
  private final NotificationRepository notificationRepository;
  private final AuthService authService;
  private final RestClient restClient;

  public HrAuthService(
    AppUserRepository appUserRepository,
    UserRoleRepository userRoleRepository,
    NotificationRepository notificationRepository,
    AuthService authService,
    RestClient.Builder restClientBuilder
  ) {
    this.appUserRepository = appUserRepository;
    this.userRoleRepository = userRoleRepository;
    this.notificationRepository = notificationRepository;
    this.authService = authService;
    this.restClient = restClientBuilder.build();
  }

  public AuthDtos.AuthResponse staffLogin(AuthDtos.StaffLoginRequest request) {
    // Step 1: authenticate against the HR system
    HrAuthResponse hrAuth;
    try {
      hrAuth = restClient.post()
        .uri(HR_LOGIN_URL)
        .body(new HrAuthRequest(request.username().trim(), request.password()))
        .retrieve()
        .body(HrAuthResponse.class);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "Staff authentication service is temporarily unavailable. Please try again later.");
    }

    if (hrAuth == null || hrAuth.token() == null || hrAuth.token().isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid staff credentials");
    }

    // Step 2: fetch staff metadata using the HR token
    HrMetadataResponse metadata;
    try {
      metadata = restClient.get()
        .uri(HR_METADATA_URL)
        .header("Authorization", "Bearer " + hrAuth.token())
        .header("Accept", "application/json")
        .retrieve()
        .body(HrMetadataResponse.class);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "Failed to retrieve staff profile. Please try again.");
    }

    if (metadata == null
      || metadata.response() == null
      || metadata.response().data() == null) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to retrieve staff profile");
    }

    HrStaffData staff = metadata.response().data();
    if (staff.status() != 1) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
        "Your staff account is not active. Please contact HR.");
    }

    AppUser user = provisionUser(request.username().trim(), staff);
    return authService.authResponse(user);
  }

  private AppUser provisionUser(String loginUsername, HrStaffData staff) {
    String manNumber = staff.manNumber() != null ? staff.manNumber().trim() : null;
    String fullName = buildFullName(staff.title(), staff.firstName(), staff.otherNames(), staff.lastName());
    String email = staff.email() != null ? staff.email().trim() : loginUsername;
    String phone = staff.phone();
    String position = staff.position() != null ? toTitleCase(staff.position().trim()) : null;

    // Search by man_number first, then fall back to email
    Optional<AppUser> existing = manNumber != null
      ? appUserRepository.findByUnzaId(manNumber)
      : appUserRepository.findByEmailIgnoreCase(email);

    if (existing.isPresent()) {
      AppUser user = existing.get();
      user.fullName = fullName;
      user.email = email;
      if (phone != null && !phone.isBlank()) user.phone = phone.trim();
      user.institution = "University of Zambia";
      if (position != null && !position.isBlank()) user.jobTitle = position;
      if (manNumber != null && user.unzaId == null) user.unzaId = manNumber;
      user.authType = AuthType.HR;
      user.lastLoginAt = Instant.now();
      return appUserRepository.save(user);
    }

    AppUser user = new AppUser();
    user.unzaId = manNumber;
    user.fullName = fullName;
    user.email = appUserRepository.existsByEmailIgnoreCase(email)
      ? (manNumber != null ? manNumber : loginUsername) + "@hr.unza.zm"
      : email;
    user.authType = AuthType.HR;
    user.status = "active";
    user.institution = "University of Zambia";
    if (phone != null && !phone.isBlank()) user.phone = phone.trim();
    if (position != null && !position.isBlank()) user.jobTitle = position;
    user.lastLoginAt = Instant.now();
    user = appUserRepository.save(user);

    UserRoleAssignment role = new UserRoleAssignment();
    role.user = user;
    role.role = AppRole.STAFF;
    userRoleRepository.save(role);
    // See OapAuthService for why this line matters — keeps AppUser.roles in sync so the
    // login response built later in this same request isn't built from a stale empty set.
    user.roles.add(role);

    Notification welcome = new Notification();
    welcome.user = user;
    welcome.title = "Welcome to the Sanitation Hub";
    welcome.message = "Signed in with your UNZA staff credentials. Contact an administrator to adjust your access level.";
    notificationRepository.save(welcome);

    return user;
  }

  private String buildFullName(String title, String first, String other, String last) {
    StringBuilder sb = new StringBuilder();
    // title is kept for completeness but omitted from display name
    if (first != null && !first.isBlank()) sb.append(toTitleCase(first.trim()));
    if (other != null && !other.isBlank()) sb.append(" ").append(toTitleCase(other.trim()));
    if (last != null && !last.isBlank()) sb.append(" ").append(toTitleCase(last.trim()));
    String name = sb.toString().trim();
    return name.isBlank() ? "UNZA Staff" : name;
  }

  private String toTitleCase(String value) {
    if (value == null || value.isBlank()) return value;
    String[] words = value.toLowerCase().split("\\s+");
    StringBuilder sb = new StringBuilder();
    for (String word : words) {
      if (!word.isEmpty()) {
        if (!sb.isEmpty()) sb.append(" ");
        sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
      }
    }
    return sb.toString();
  }

  // ── HR API DTOs ───────────────────────────────────────────────────────────────
  // Jackson SNAKE_CASE strategy maps man_number → manNumber, other_names → otherNames etc.

  private record HrAuthRequest(String username, String password) {}

  private record HrAuthResponse(String message, String token, Integer expiresIn, String tokenType) {}

  private record HrMetadataResponse(HrMetadataWrapper response) {}

  private record HrMetadataWrapper(int status, String message, HrStaffData data) {}

  private record HrStaffData(
    String manNumber,
    String title,
    String firstName,
    String lastName,
    String otherNames,
    String email,
    String phone,
    String position,
    String positionHierarchy,
    String profileImage,
    int status
  ) {}
}
