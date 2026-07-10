package com.unza.ubirr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
  name = "hero_banners",
  indexes = { @Index(name = "idx_banner_order", columnList = "display_order") }
)
public class HeroBanner {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @Column(nullable = false)
  public String title;

  @Column(columnDefinition = "TEXT")
  public String subtitle;

  @Column
  public String imagePath;

  @Column
  public String linkUrl;

  @Column(name = "display_order")
  public int displayOrder = 0;

  @Column(nullable = false)
  public boolean active = true;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  public Instant updatedAt;
}
