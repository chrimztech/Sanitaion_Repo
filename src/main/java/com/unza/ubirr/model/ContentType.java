package com.unza.ubirr.model;

public enum ContentType {
  ARTICLE,
  EVENT,
  VIDEO,
  AUDIO,
  PDF,
  IMAGE,
  GALLERY,
  DIGITIZED,
  ACADEMIC,
  EDUCATIONAL,
  ANNOUNCEMENT,
  PAGE,
  REPORT,
  GUIDELINES,
  POLICY,
  TRAINING_MATERIAL,
  RESEARCH_DATA,
  CASE_STUDY,
  STANDARDS,
  MANUAL,
  PRESENTATION,
  SPREADSHEET,
  DATASET,
  THESIS,
  JOURNAL_ARTICLE;

  public String apiValue() {
    return name().toLowerCase();
  }
}
