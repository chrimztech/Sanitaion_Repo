package com.unza.ubirr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
  name = "users",
  indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_unza_id", columnList = "unza_id"),
    @Index(name = "idx_users_status", columnList = "status"),
    @Index(name = "idx_users_auth_type", columnList = "auth_type")
  }
)
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @Column(nullable = false, unique = true)
  public String email;

  @Column
  public String passwordHash;

  @Column(nullable = false)
  public String fullName = "";

  @Column
  public String phone;

  @Column(unique = true)
  public String unzaId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "auth_type")
  public AuthType authType = AuthType.LOCAL;

  @Column
  public String affiliation;

  @Column
  public String institution;

  @Column
  public String jobTitle;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  public Department department;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "school_id")
  public School school;

  @Column(nullable = false)
  public String status = "active";

  @Column
  public Instant lastLoginAt;

  @Column
  public String resetToken;

  @Column
  public Instant resetTokenExpiresAt;

  @Column(nullable = false, columnDefinition = "integer not null default 0")
  public int loginAttempts = 0;

  @Column
  public Instant lockedUntil;

  @Column(nullable = false, columnDefinition = "boolean not null default false")
  public boolean emailVerified = false;

  @Column
  public String emailVerificationToken;

  /** Rotated on every new login. JWT must carry this value as "sid"; mismatches are rejected. */
  @Column(name = "current_session_id")
  public String currentSessionId;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public Instant updatedAt;

  @OneToMany(mappedBy = "user", orphanRemoval = true)
  public Set<UserRoleAssignment> roles = new LinkedHashSet<>();
}
