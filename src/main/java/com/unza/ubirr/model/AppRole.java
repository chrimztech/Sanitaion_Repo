package com.unza.ubirr.model;

public enum AppRole {
  SUPER_ADMIN,
  PROJECT_ADMIN,
  CONTENT_MANAGER,
  DIGITIZATION_OFFICER,
  REVIEWER,
  STAFF,
  STUDENT;

  public String apiValue() {
    return name().toLowerCase();
  }
}
