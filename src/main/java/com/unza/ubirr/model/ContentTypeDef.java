package com.unza.ubirr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "content_type_defs")
public class ContentTypeDef {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  @Column(nullable = false, unique = true)
  public String name;

  @Column(nullable = false, unique = true)
  public String slug;

  @Column(columnDefinition = "TEXT")
  public String description;
}
