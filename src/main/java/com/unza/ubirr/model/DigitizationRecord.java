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
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "digitization_records")
public class DigitizationRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @Column(nullable = false)
  public String title;

  @Column(columnDefinition = "TEXT")
  public String description;

  @Column(columnDefinition = "TEXT")
  public String sourceOwner;

  @Column(columnDefinition = "TEXT")
  public String physicalLocation;

  @Column
  public Integer numberOfPages;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public DigitizationStatus digitizationStatus = DigitizationStatus.PENDING;

  @Column
  public String scannedFilePath;

  @Column(columnDefinition = "TEXT")
  public String ocrText;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "scanned_by")
  public AppUser scannedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by")
  public AppUser reviewedBy;

  @Column
  public String qualityStatus;

  @Column(columnDefinition = "TEXT")
  public String copyrightStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_item_id")
  public ContentItem contentItem;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public Instant updatedAt;
}
