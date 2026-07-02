package com.unza.ubirr.model;

public enum DigitizationStatus {
  PENDING,
  SCANNING,
  SCANNED,
  OCR_DONE,
  METADATA_ADDED,
  UNDER_REVIEW,
  APPROVED,
  PUBLISHED,
  REJECTED,
  RESCAN;

  public String apiValue() {
    return name().toLowerCase();
  }
}
