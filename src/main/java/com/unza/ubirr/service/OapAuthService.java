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
public class OapAuthService {

  private static final String OAP_URL = "https://devoap.unza.zm/api/v1/customers/login";

  private final AppUserRepository appUserRepository;
  private final UserRoleRepository userRoleRepository;
  private final NotificationRepository notificationRepository;
  private final AuthService authService;
  private final RestClient restClient;

  public OapAuthService(
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

  public AuthDtos.AuthResponse studentLogin(AuthDtos.StudentLoginRequest request) {
    OapResponse oapResponse;
    try {
      oapResponse = restClient.post()
        .uri(OAP_URL)
        .body(new OapRequest(request.username().trim(), request.password(), request.instance().trim().toLowerCase()))
        .retrieve()
        .body(OapResponse.class);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "Student authentication service is temporarily unavailable. Please try again later.");
    }

    if (oapResponse == null || !oapResponse.success() || oapResponse.data() == null || oapResponse.data().user() == null) {
      String msg = (oapResponse != null && oapResponse.message() != null)
        ? oapResponse.message()
        : "Invalid student credentials";
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg);
    }

    AppUser user = provisionUser(oapResponse.data().user());
    return authService.authResponse(user);
  }

  private AppUser provisionUser(OapUserData oapUser) {
    String username = oapUser.username() != null ? oapUser.username().trim() : String.valueOf(oapUser.studentId());
    String fullName = buildFullName(oapUser.firstName(), oapUser.middleName(), oapUser.lastName());
    String email = resolveEmail(oapUser.email(), username);
    String phone = oapUser.phoneNumber();
    // school = faculty within UNZA → stored as affiliation
    // program = degree programme → stored as jobTitle (closest available field)
    String school = oapUser.school() != null ? toTitleCase(oapUser.school().trim()) : null;
    String program = oapUser.program() != null ? toTitleCase(oapUser.program().trim()) : null;

    Optional<AppUser> existing = appUserRepository.findByUnzaId(username);
    if (existing.isPresent()) {
      AppUser user = existing.get();
      user.fullName = fullName;
      user.email = resolveEmailForExistingUser(email, user.email);
      if (phone != null && !phone.isBlank()) user.phone = phone.trim();
      user.institution = "University of Zambia";
      if (school != null && !school.isBlank()) user.affiliation = school;
      if (program != null && !program.isBlank()) user.jobTitle = program;
      user.lastLoginAt = Instant.now();
      return appUserRepository.save(user);
    }

    AppUser user = new AppUser();
    user.unzaId = username;
    user.fullName = fullName;
    user.email = appUserRepository.existsByEmailIgnoreCase(email)
      ? username + "@oap.unza.zm"
      : email;
    user.authType = AuthType.OAP;
    user.status = "active";
    user.institution = "University of Zambia";
    if (phone != null && !phone.isBlank()) user.phone = phone.trim();
    if (school != null && !school.isBlank()) user.affiliation = school;
    if (program != null && !program.isBlank()) user.jobTitle = program;
    user.lastLoginAt = Instant.now();
    user = appUserRepository.save(user);

    UserRoleAssignment role = new UserRoleAssignment();
    role.user = user;
    role.role = AppRole.STUDENT;
    userRoleRepository.save(role);

    Notification welcome = new Notification();
    welcome.user = user;
    welcome.title = "Welcome to the Sanitation Hub";
    welcome.message = "Signed in with your UNZA student credentials. You can now access resources and bookmark content.";
    notificationRepository.save(welcome);

    return user;
  }

  private String resolveEmailForExistingUser(String oapEmail, String existingEmail) {
    // Keep existing email if OAP email is garbage
    if (oapEmail == null || oapEmail.isBlank() || !oapEmail.contains("@")) return existingEmail;
    return oapEmail;
  }

  private String buildFullName(String first, String middle, String last) {
    StringBuilder sb = new StringBuilder();
    if (first != null && !first.isBlank()) sb.append(first.trim());
    if (middle != null && !middle.isBlank()) sb.append(" ").append(middle.trim());
    if (last != null && !last.isBlank()) sb.append(" ").append(last.trim());
    String name = sb.toString().trim();
    return name.isBlank() ? "UNZA Student" : name;
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

  private String resolveEmail(String email, String username) {
    if (email == null || email.isBlank()) return username + "@students.unza.zm";
    // Fix common OAP typos: comma instead of period in domain (e.g. user@unza,zm)
    String cleaned = email.trim().replace(",", ".");
    return cleaned.contains("@") ? cleaned : username + "@students.unza.zm";
  }

  // ── OAP API DTOs ──────────────────────────────────────────────────────────────
  // Jackson SNAKE_CASE naming strategy maps first_name → firstName etc. automatically.

  private record OapRequest(String username, String password, String instance) {}

  private record OapResponse(boolean success, String message, OapData data) {}

  private record OapData(OapUserData user) {}

  private record OapUserData(
    long id,
    String username,
    String email,
    String firstName,
    String lastName,
    String middleName,
    long studentId,
    String userType,
    String phoneNumber,
    String gender,
    String program,
    int yearOfProgram,
    String school,
    String schoolCode,
    String schoolLocation,
    String instance
  ) {}
}
