package com.unza.ubirr.model;

public enum ContentStatus {
  DRAFT,
  SUBMITTED,
  RETURNED,
  APPROVED,
  PUBLISHED,
  COMPLETED,
  ARCHIVED,
  REJECTED;

  public String apiValue() {
    return name().toLowerCase();
  }
}
