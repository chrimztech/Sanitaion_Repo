package com.unza.ubirr.service;

import com.unza.ubirr.model.AppRole;
import com.unza.ubirr.model.AppUser;
import com.unza.ubirr.repository.AppUserRepository;
import com.unza.ubirr.security.UserPrincipal;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthContextService {
  private final AppUserRepository appUserRepository;

  public AuthContextService(AppUserRepository appUserRepository) {
    this.appUserRepository = appUserRepository;
  }

  public Optional<AppUser> currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
      return Optional.empty();
    }
    return appUserRepository.findByIdWithRoles(principal.id());
  }

  public AppUser requireUser() {
    return currentUser().orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authentication required"));
  }

  public UUID currentUserIdOrNull() {
    return currentUser().map(user -> user.id).orElse(null);
  }

  public void requireAdmin() {
    AppUser user = requireUser();
    if (!hasAnyRole(user, AppRole.SUPER_ADMIN, AppRole.PROJECT_ADMIN)) {
      throw new ResponseStatusException(FORBIDDEN, "Administrator access required");
    }
  }

  public void requireContentManager() {
    AppUser user = requireUser();
    if (!canManageContent(user)) {
      throw new ResponseStatusException(FORBIDDEN, "Content management access required");
    }
  }

  public boolean canManageContent(AppUser user) {
    return hasAnyRole(
      user,
      AppRole.SUPER_ADMIN,
      AppRole.PROJECT_ADMIN,
      AppRole.CONTENT_MANAGER,
      AppRole.DIGITIZATION_OFFICER,
      AppRole.REVIEWER
    );
  }

  public boolean isAdmin(AppUser user) {
    return hasAnyRole(user, AppRole.SUPER_ADMIN, AppRole.PROJECT_ADMIN);
  }

  public boolean hasAnyRole(AppUser user, AppRole... roles) {
    EnumSet<AppRole> expected = EnumSet.noneOf(AppRole.class);
    for (AppRole role : roles) {
      expected.add(role);
    }
    return user.roles.stream().map(item -> item.role).anyMatch(expected::contains);
  }
}
