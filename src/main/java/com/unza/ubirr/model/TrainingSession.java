package com.unza.ubirr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "training_sessions")
public class TrainingSession {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @Column(nullable = false)
  public String title;

  @Column(nullable = false, unique = true)
  public String slug;

  @Column(columnDefinition = "TEXT")
  public String description;

  @Column
  public String mode; // online, in-person, hybrid

  @Column
  public String venue;

  @Column
  public String instructor;

  @Column(nullable = false)
  public LocalDate startDate;

  @Column
  public LocalDate endDate;

  @Column
  public LocalTime startTime;

  @Column
  public LocalTime endTime;

  @Column
  public Integer capacity;

  @Column(columnDefinition = "TEXT")
  public String requirements;

  @Column
  public String contactEmail;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public ContentStatus status = ContentStatus.DRAFT;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  public AppUser createdBy;

  @Column
  public Instant publishedAt;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public Instant updatedAt;
}
