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
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// A periodic M&E visit record for a demonstration site — the concept note calls for
// "periodic monitoring, evaluation and research" at real-life sites and "feedback loops
// for improvement"; this is what lets the Hub team log a visit's findings over time
// instead of the site profile only ever reflecting its current static state.
@Entity
@Table(
  name = "site_monitoring_records",
  indexes = {
    @Index(name = "idx_smr_site_id", columnList = "site_id"),
    @Index(name = "idx_smr_visit_date", columnList = "visit_date")
  }
)
public class SiteMonitoringRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "site_id", nullable = false)
  public DemonstrationSite site;

  @Column(name = "visit_date", nullable = false)
  public LocalDate visitDate;

  @Column(name = "conducted_by")
  public String conductedBy;

  // Simple controlled vocabulary: excellent | good | fair | poor | needs_attention
  @Column(name = "performance_rating")
  public String performanceRating;

  @Column(columnDefinition = "TEXT")
  public String findings;

  @Column(columnDefinition = "TEXT")
  public String recommendations;

  @Column(name = "follow_up_required", nullable = false)
  public boolean followUpRequired = false;

  @Column(name = "follow_up_date")
  public LocalDate followUpDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recorded_by")
  public AppUser recordedBy;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public Instant updatedAt;
}
