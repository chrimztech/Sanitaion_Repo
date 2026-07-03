package com.unza.ubirr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
  name = "demonstration_sites",
  indexes = {
    @Index(name = "idx_ds_slug", columnList = "slug"),
    @Index(name = "idx_ds_status", columnList = "operational_status"),
    @Index(name = "idx_ds_site_type", columnList = "site_type")
  }
)
public class DemonstrationSite {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @Column(nullable = false)
  public String name;

  @Column(nullable = false, unique = true)
  public String slug;

  @Column(columnDefinition = "TEXT")
  public String description;

  @Column
  public String location;

  @Column
  public String district;

  @Column(name = "site_type")
  public String siteType;

  @Column
  public String technology;

  @Column(name = "operational_status")
  public String operationalStatus = "active";

  @Column
  public LocalDate operationalSince;

  @Column
  public String contactPerson;

  @Column
  public String contactEmail;

  @Column
  public String partnerInstitution;

  @Column
  public Double latitude;

  @Column
  public Double longitude;

  @Column(columnDefinition = "TEXT")
  public String notes;

  @Column
  public String imagePath;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public Instant updatedAt;
}
