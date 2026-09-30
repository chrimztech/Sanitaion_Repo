package com.unza.ubirr.dto;

import com.unza.ubirr.dto.PublicDtos.ResourceSummaryDto;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class AccountDtos {
  private AccountDtos() {
  }

  public record ProfileDto(
    UUID id,
    String email,
    String fullName,
    String phone,
    String affiliation,
    String institution,
    String jobTitle,
    String status,
    Instant createdAt,
    Instant lastLoginAt,
    List<String> roles
  ) {
  }

  public record UpdateProfileRequest(
    @NotBlank(message = "Full name is required")
    String fullName,
    String phone,
    String institution,
    String jobTitle
  ) {
  }

  public record BookmarkStatusDto(
    boolean saved
  ) {
  }

  public record BookmarkDto(
    UUID id,
    Instant createdAt,
    ResourceSummaryDto content
  ) {
  }

  public record RecentViewDto(
    Instant viewedAt,
    ResourceSummaryDto content
  ) {
  }

  public record NotificationDto(
    UUID id,
    String title,
    String message,
    String link,
    boolean read,
    Instant createdAt
  ) {
  }

  public record MarkNotificationsReadRequest(
    boolean all,
    List<UUID> ids
  ) {
  }

  public record ReadingListItemDto(
    UUID id,
    UUID contentId,
    String title,
    String slug,
    String note,
    int position,
    Instant addedAt
  ) {
  }

  public record ReadingListDto(
    UUID id,
    String name,
    String description,
    boolean isPublic,
    Instant createdAt,
    Instant updatedAt,
    List<ReadingListItemDto> items
  ) {
  }

  public record SaveReadingListRequest(
    @NotBlank(message = "Name is required")
    String name,
    String description,
    boolean isPublic
  ) {
  }

  public record SaveReadingListItemRequest(
    UUID contentId,
    String note,
    Integer position
  ) {
  }

  public record TrainingSessionPublicDto(
    UUID id,
    String title,
    String slug,
    String description,
    String mode,
    String venue,
    String instructor,
    LocalDate startDate,
    LocalDate endDate,
    LocalTime startTime,
    LocalTime endTime,
    Integer capacity,
    String requirements,
    String contactEmail,
    String status,
    Instant publishedAt,
    long registrationCount,
    boolean registered,
    String imageUrl
  ) {
  }

  public record TrainingRegistrationDto(
    UUID id,
    UUID sessionId,
    String sessionTitle,
    String sessionSlug,
    LocalDate sessionStartDate,
    String status,
    Instant registeredAt
  ) {
  }
}
