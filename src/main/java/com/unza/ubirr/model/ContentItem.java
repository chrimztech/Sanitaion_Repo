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
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
  name = "content_items",
  indexes = {
    @Index(name = "idx_ci_slug", columnList = "slug"),
    @Index(name = "idx_ci_status", columnList = "status"),
    @Index(name = "idx_ci_content_type", columnList = "content_type"),
    @Index(name = "idx_ci_access_level", columnList = "access_level"),
    @Index(name = "idx_ci_published_at", columnList = "published_at"),
    @Index(name = "idx_ci_category_id", columnList = "category_id"),
    @Index(name = "idx_ci_department_id", columnList = "department_id"),
    @Index(name = "idx_ci_author_id", columnList = "author_id"),
    @Index(name = "idx_ci_deleted_at", columnList = "deleted_at"),
    @Index(name = "idx_ci_status_published", columnList = "status,published_at")
  }
)
public class ContentItem {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @Column(nullable = false)
  public String title;

  @Column(nullable = false, unique = true)
  public String slug;

  @Column(columnDefinition = "TEXT")
  public String summary;

  @Column(columnDefinition = "TEXT")
  public String body;

  @Column(nullable = false, name = "content_type")
  public String contentType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  public Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  public Department department;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  public AppUser author;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "access_level")
  public AccessLevel accessLevel = AccessLevel.DRAFT;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public ContentStatus status = ContentStatus.DRAFT;

  @Column
  public String thumbnailPath;

  @Column(nullable = false)
  public boolean downloadable = true;

  @Column(columnDefinition = "TEXT")
  public String copyrightStatus;

  @Column(columnDefinition = "TEXT")
  public String licenceInformation;

  @Column
  public String language = "en";

  @Column(columnDefinition = "TEXT")
  public String keywords;

  @Column(columnDefinition = "TEXT")
  public String authors;

  @Column(columnDefinition = "TEXT")
  public String publisher;

  // Year the resource was originally published (may differ from system publishedAt). Most
  // library/citation metadata only ever records a year, not a specific day — storing a
  // plain year instead of a fabricated LocalDate avoids pretending to know a day/month
  // that was never actually given.
  @Column
  public Integer publicationYear;

  // Date the activity/event described in the resource took place
  @Column
  public LocalDate activityDate;

  // Institution or organisation that produced this resource
  @Column(columnDefinition = "TEXT")
  public String sourceInstitution;

  // DOI, ISBN, or other external identifier
  @Column(columnDefinition = "TEXT")
  public String externalIdentifier;

  // Volume/issue/page range for journal articles
  @Column(columnDefinition = "TEXT")
  public String bibliographicInfo;

  @Column
  public Instant publishedAt;

  @Column
  public Instant expiresAt;

  @Column(nullable = false)
  public int viewCount = 0;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public Instant updatedAt;

  @Column
  public Instant deletedAt;

  // Maintained automatically by DB trigger; never written by the application.
  // Used by fullTextSearch() native query for GIN-indexed FTS.
  @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
  public String searchVector;
}
