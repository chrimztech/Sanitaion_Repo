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
@Table(name = "content_files")
public class ContentFile {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_item_id", nullable = false)
  public ContentItem contentItem;

  @Column(nullable = false)
  public String originalFileName;

  @Column(nullable = false)
  public String storedFileName;

  @Column(nullable = false)
  public String filePath;

  @Column
  public String fileType;

  @Column
  public String mimeType;

  @Column
  public String fileExtension;

  @Column
  public Long fileSize;

  @Column
  public String checksum;

  @Column(nullable = false)
  public int versionNumber = 1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by")
  public AppUser uploadedBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public AccessLevel accessLevel = AccessLevel.DRAFT;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public Instant updatedAt;
}
