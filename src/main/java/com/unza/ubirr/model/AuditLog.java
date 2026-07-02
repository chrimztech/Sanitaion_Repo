package com.unza.ubirr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  public AppUser user;

  @Column(nullable = false)
  public String action;

  @Column
  public String entityType;

  @Column
  public String entityId;

  @Column(columnDefinition = "TEXT")
  public String oldValue;

  @Column(columnDefinition = "TEXT")
  public String newValue;

  @Column
  public String ipAddress;

  @Column
  public String userAgent;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant createdAt;
}
