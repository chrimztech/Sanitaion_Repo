package com.unza.ubirr.service;

import com.unza.ubirr.config.LdapProperties;
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
import java.util.List;
import java.util.Optional;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class LdapAuthService {

  private final LdapProperties ldapProperties;
  private final AppUserRepository appUserRepository;
  private final UserRoleRepository userRoleRepository;
  private final NotificationRepository notificationRepository;
  private final AuthService authService;

  public LdapAuthService(
    LdapProperties ldapProperties,
    AppUserRepository appUserRepository,
    UserRoleRepository userRoleRepository,
    NotificationRepository notificationRepository,
    AuthService authService
  ) {
    this.ldapProperties = ldapProperties;
    this.appUserRepository = appUserRepository;
    this.userRoleRepository = userRoleRepository;
    this.notificationRepository = notificationRepository;
    this.authService = authService;
  }

  public AuthDtos.AuthResponse ldapLogin(AuthDtos.LdapLoginRequest request) {
    if (!ldapProperties.isEnabled()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "LDAP authentication is not enabled on this server");
    }

    String unzaId = request.unzaId().trim().toLowerCase();
    LdapContextSource contextSource = buildContextSource();
    LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
    ldapTemplate.setIgnorePartialResultException(true);

    // authenticate() returns void and throws on failure
    LdapQuery userQuery = LdapQueryBuilder.query()
      .base(ldapProperties.getUserSearchBase())
      .filter(ldapProperties.getUserSearchFilter().replace("{0}", unzaId));

    try {
      ldapTemplate.authenticate(userQuery, request.password());
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid UNZA credentials");
    }

    LdapUserAttributes attrs = fetchAttributes(ldapTemplate, unzaId);
    AppUser user = provisionUser(unzaId, attrs);
    return authService.authResponse(user);
  }

  private LdapUserAttributes fetchAttributes(LdapTemplate ldapTemplate, String unzaId) {
    try {
      LdapQuery query = LdapQueryBuilder.query()
        .base(ldapProperties.getUserSearchBase())
        .filter(ldapProperties.getUserSearchFilter().replace("{0}", unzaId));

      // search() with AttributesMapper returns a List
      List<LdapUserAttributes> results = ldapTemplate.search(
        query,
        (AttributesMapper<LdapUserAttributes>) attrs -> mapAttributes(attrs, unzaId)
      );
      return results.isEmpty() ? fallback(unzaId) : results.get(0);
    } catch (Exception e) {
      return fallback(unzaId);
    }
  }

  private LdapUserAttributes mapAttributes(Attributes attrs, String unzaId) throws NamingException {
    String email = getAttribute(attrs, ldapProperties.getEmailAttribute(), unzaId + "@unza.zm");
    String name = getAttribute(attrs, ldapProperties.getNameAttribute(), unzaId);
    return new LdapUserAttributes(email, name, unzaId);
  }

  private String getAttribute(Attributes attrs, String attrName, String fallback) throws NamingException {
    var attr = attrs.get(attrName);
    if (attr == null) return fallback;
    Object val = attr.get();
    return val != null ? val.toString() : fallback;
  }

  private LdapUserAttributes fallback(String unzaId) {
    return new LdapUserAttributes(unzaId + "@unza.zm", unzaId, unzaId);
  }

  private AppUser provisionUser(String unzaId, LdapUserAttributes attrs) {
    Optional<AppUser> existing = appUserRepository.findByUnzaId(unzaId);
    if (existing.isPresent()) {
      AppUser user = existing.get();
      user.fullName = attrs.fullName();
      user.email = attrs.email();
      user.lastLoginAt = Instant.now();
      return appUserRepository.save(user);
    }

    AppUser user = new AppUser();
    user.email = appUserRepository.existsByEmailIgnoreCase(attrs.email())
      ? unzaId + "@ldap.unza.zm"
      : attrs.email();
    user.fullName = attrs.fullName();
    user.unzaId = unzaId;
    user.authType = AuthType.LDAP;
    user.status = "active";
    user.lastLoginAt = Instant.now();
    user = appUserRepository.save(user);

    UserRoleAssignment role = new UserRoleAssignment();
    role.user = user;
    role.role = AppRole.STAFF;
    userRoleRepository.save(role);

    Notification welcome = new Notification();
    welcome.user = user;
    welcome.title = "Welcome to the Sanitation Hub";
    welcome.message = "Signed in with your UNZA credentials. Contact admin to adjust your access.";
    notificationRepository.save(welcome);

    return user;
  }

  private LdapContextSource buildContextSource() {
    LdapContextSource source = new LdapContextSource();
    source.setUrl(ldapProperties.getUrl());
    if (ldapProperties.getBaseDn() != null && !ldapProperties.getBaseDn().isBlank()) {
      source.setBase(ldapProperties.getBaseDn());
    }
    if (ldapProperties.getManagerDn() != null && !ldapProperties.getManagerDn().isBlank()) {
      source.setUserDn(ldapProperties.getManagerDn());
      source.setPassword(ldapProperties.getManagerPassword());
    }
    try {
      source.afterPropertiesSet();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "LDAP server is unreachable. Please contact the system administrator.");
    }
    return source;
  }

  private record LdapUserAttributes(String email, String fullName, String unzaId) {}
}
