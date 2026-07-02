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

@Entity
@Table(name = "approval_workflows")
public class ApprovalWorkflow {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_item_id", nullable = false)
  public ContentItem contentItem;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "submitted_by")
  public AppUser submittedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by")
  public AppUser reviewedBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public ContentStatus status;

  @Column(columnDefinition = "TEXT")
  public String reviewerComments;

  @Column(nullable = false)
  public Instant submittedAt = Instant.now();

  @Column
  public Instant reviewedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "published_by")
  public AppUser publishedBy;

  @Column
  public Instant publishedAt;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant createdAt;
}
