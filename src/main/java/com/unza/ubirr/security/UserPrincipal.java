package com.unza.ubirr.security;

import com.unza.ubirr.model.AppRole;
import com.unza.ubirr.model.AppUser;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
  private final UUID id;
  private final String email;
  private final String password;
  private final List<AppRole> roles;
  private final String sessionId;

  public UserPrincipal(UUID id, String email, String password, List<AppRole> roles, String sessionId) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.roles = roles;
    this.sessionId = sessionId;
  }

  public static UserPrincipal from(AppUser user) {
    return new UserPrincipal(
      user.id,
      user.email,
      user.passwordHash,
      user.roles.stream().map(role -> role.role).toList(),
      user.currentSessionId
    );
  }

  public UUID id() { return id; }
  public List<AppRole> roles() { return roles; }
  public String sessionId() { return sessionId; }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
