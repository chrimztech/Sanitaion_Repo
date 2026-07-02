package com.unza.ubirr.service;

import com.unza.ubirr.model.AccessLevel;
import com.unza.ubirr.model.AppRole;
import com.unza.ubirr.model.ContentStatus;
import com.unza.ubirr.model.ContentType;
import com.unza.ubirr.model.DigitizationStatus;
import java.util.Locale;

public final class EnumMapper {
  private EnumMapper() {
  }

  public static AppRole role(String value) {
    return value == null ? AppRole.STUDENT : AppRole.valueOf(normalize(value));
  }

  public static AccessLevel access(String value) {
    return value == null ? AccessLevel.DRAFT : AccessLevel.valueOf(normalize(value));
  }

  public static ContentStatus status(String value) {
    return value == null ? ContentStatus.DRAFT : ContentStatus.valueOf(normalize(value));
  }

  public static ContentType contentType(String value) {
    return value == null ? ContentType.ARTICLE : ContentType.valueOf(normalize(value));
  }

  public static DigitizationStatus digitizationStatus(String value) {
    return value == null ? DigitizationStatus.PENDING : DigitizationStatus.valueOf(normalize(value));
  }

  public static String normalizeSlugValue(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  public static String slugify(String value) {
    return normalizeSlugValue(value)
      .replaceAll("[^a-z0-9]+", "-")
      .replaceAll("^-|-$", "")
      .substring(0, Math.min(normalizeSlugValue(value)
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("^-|-$", "").length(), 80));
  }

  private static String normalize(String value) {
    return value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
  }
}
