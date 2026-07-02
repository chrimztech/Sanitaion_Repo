package com.unza.ubirr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
  name = "user_sessions",
  indexes = {
    @Index(name = "idx_us_session_id", columnList = "session_id"),
    @Index(name = "idx_us_user_id",    columnList = "user_id"),
  }
)
public class UserSession {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @Column(name = "session_id", nullable = false)
  public String sessionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  public AppUser user;

  @Column(name = "ip_address")
  public String ipAddress;

  @Column(name = "user_agent", length = 500)
  public String userAgent;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  public Instant createdAt;

  @Column(name = "expires_at", nullable = false)
  public Instant expiresAt;

  /** Set when the session is ended by logout or by admin revocation. */
  @Column(name = "revoked_at")
  public Instant revokedAt;
}
