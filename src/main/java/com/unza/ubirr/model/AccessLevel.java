package com.unza.ubirr.model;

public enum AccessLevel {
  PUBLIC,
  UNIVERSITY,
  STAFF,
  STUDENT,
  CORE_TEAM,
  DRAFT;

  public String apiValue() {
    return name().toLowerCase();
  }
}
