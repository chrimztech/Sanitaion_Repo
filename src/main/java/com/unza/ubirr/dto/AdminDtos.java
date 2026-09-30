package com.unza.ubirr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminDtos {
  private AdminDtos() {
  }

  public record RecentContentDto(
    UUID id,
    String title,
    String status,
    Instant updatedAt
  ) {
  }

  public record DashboardSummaryDto(
    long totalResources,
    long publishedResources,
    long draftResources,
    long pendingApprovalResources,
    long publishedEvents,
    long digitizationRecords,
    List<RecentContentDto> recentContent
  ) {
  }

  public record ContentEditorDto(
    UUID id,
    String title,
    String slug,
    String summary,
    String body,
    String contentType,
    String status,
    String accessLevel,
    boolean downloadable,
    String copyrightStatus,
    String licenceInformation,
    String language,
    String keywords,
    String authors,
    String publisher,
    LocalDate publicationDate,
    LocalDate activityDate,
    String sourceInstitution,
    String externalIdentifier,
    String bibliographicInfo,
    String categoryId,
    String departmentId,
    String authorId,
    Instant publishedAt,
    String thumbnailUrl
  ) {
  }

  public record SaveContentRequest(
    @NotBlank(message = "Title is required")
    String title,
    String slug,
    String summary,
    String body,
    String contentType,
    String accessLevel,
    String status,
    Boolean downloadable,
    String copyrightStatus,
    String licenceInformation,
    String language,
    String keywords,
    String authors,
    String publisher,
    LocalDate publicationDate,
    LocalDate activityDate,
    String sourceInstitution,
    String externalIdentifier,
    String bibliographicInfo,
    String categoryId,
    String departmentId
  ) {
  }

  public record ContentTransitionRequest(
    @NotBlank(message = "Action is required")
    String action,
    String comments
  ) {
  }

  public record ApprovalItemDto(
    UUID id,
    String title,
    String slug,
    String contentType,
    String status,
    Instant updatedAt,
    String summary
  ) {
  }

  public record EventAdminDto(
    UUID id,
    String title,
    String slug,
    String description,
    String venue,
    LocalDate startDate,
    LocalDate endDate,
    LocalTime startTime,
    LocalTime endTime,
    String organizer,
    String contactEmail,
    String registrationLink,
    String accessLevel,
    String status,
    Instant publishedAt,
    String bannerImageUrl
  ) {
  }

  public record SaveEventRequest(
    @NotBlank(message = "Title is required")
    String title,
    String slug,
    String description,
    String venue,
    LocalDate startDate,
    LocalDate endDate,
    LocalTime startTime,
    LocalTime endTime,
    String organizer,
    String contactEmail,
    String registrationLink,
    String accessLevel,
    String status
  ) {
  }

  public record DigitizationDto(
    UUID id,
    String title,
    String description,
    String sourceOwner,
    String physicalLocation,
    Integer numberOfPages,
    String digitizationStatus,
    String scannedFilePath,
    String scanFileUrl,
    String ocrText,
    String qualityStatus,
    String copyrightStatus,
    UUID contentItemId,
    String scannedByName,
    String reviewedByName,
    Instant createdAt,
    Instant updatedAt
  ) {
  }

  public record SaveDigitizationRequest(
    @NotBlank(message = "Title is required")
    String title,
    String description,
    String sourceOwner,
    String physicalLocation,
    Integer numberOfPages,
    String digitizationStatus,
    String scannedFilePath,
    String ocrText,
    String qualityStatus,
    String copyrightStatus,
    UUID contentItemId
  ) {
  }

  public record UserAdminDto(
    UUID id,
    String email,
    String fullName,
    String status,
    String authType,
    Instant createdAt,
    Instant lastLoginAt,
    List<String> roles
  ) {
  }

  public record UpdateUserRolesRequest(
    List<String> roles
  ) {
  }

  public record UpdateUserStatusRequest(
    @NotBlank(message = "Status is required")
    String status
  ) {
  }

  public record SetPasswordRequest(
    @NotBlank(message = "New password is required")
    String password
  ) {
  }

  public record AdvanceStatusRequest(
    @NotBlank(message = "Status is required")
    String status
  ) {
  }

  public record PromoteResult(
    UUID contentItemId,
    DigitizationDto record
  ) {
  }

  public record SessionDto(
    UUID id,
    UUID userId,
    String userName,
    String userEmail,
    String ipAddress,
    String userAgent,
    Instant createdAt,
    Instant expiresAt,
    boolean active,
    Instant revokedAt
  ) {
  }

  public record TaxonomyEntryDto(
    UUID id,
    String name,
    String slug,
    String description,
    String code,
    UUID schoolId
  ) {
  }

  public record SaveTaxonomyEntryRequest(
    @NotBlank(message = "Name is required")
    String name,
    String slug,
    String description,
    String code,
    UUID schoolId
  ) {
  }

  public record SystemSettingDto(
    UUID id,
    String settingKey,
    String settingValue,
    String description
  ) {
  }

  public record UpdateSettingRequest(
    @NotBlank(message = "Value is required")
    String value
  ) {
  }

  public record AuditLogDto(
    UUID id,
    String userId,
    String userEmail,
    String action,
    String entityType,
    String entityId,
    String oldValue,
    String newValue,
    String ipAddress,
    Instant createdAt
  ) {
  }

  public record BackupDto(
    String filename,
    String type,
    long sizeBytes,
    Instant createdAt
  ) {
  }

  public record BackupLocationDto(
    String path,
    boolean isDefault,
    boolean writable
  ) {
  }

  public record UpdateBackupLocationRequest(
    // Blank/omitted resets to the default configured location (app.backup.dir)
    String path
  ) {
  }

  public record RestoreResultDto(
    String filename,
    boolean databaseRestored,
    boolean filesRestored,
    String message
  ) {
  }

  public record DemonstrationSiteDto(
    UUID id,
    String name,
    String slug,
    String description,
    String location,
    String district,
    String siteType,
    String technology,
    String operationalStatus,
    LocalDate operationalSince,
    String contactPerson,
    String contactEmail,
    String partnerInstitution,
    Double latitude,
    Double longitude,
    String notes,
    Instant createdAt,
    Instant updatedAt,
    String imageUrl
  ) {
  }

  public record SaveDemonstrationSiteRequest(
    @NotBlank(message = "Name is required")
    String name,
    String slug,
    String description,
    String location,
    String district,
    String siteType,
    String technology,
    String operationalStatus,
    LocalDate operationalSince,
    String contactPerson,
    String contactEmail,
    String partnerInstitution,
    Double latitude,
    Double longitude,
    String notes
  ) {
  }

  public record SiteMonitoringRecordDto(
    UUID id,
    UUID siteId,
    LocalDate visitDate,
    String conductedBy,
    String performanceRating,
    String findings,
    String recommendations,
    boolean followUpRequired,
    LocalDate followUpDate,
    String recordedByName,
    Instant createdAt
  ) {
  }

  public record SaveSiteMonitoringRecordRequest(
    @NotNull(message = "Visit date is required")
    LocalDate visitDate,
    String conductedBy,
    String performanceRating,
    String findings,
    String recommendations,
    boolean followUpRequired,
    LocalDate followUpDate
  ) {
  }

  public record TrainingSessionDto(
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
    Instant createdAt,
    long registrationCount,
    String imageUrl
  ) {
  }

  public record SaveTrainingSessionRequest(
    @NotBlank(message = "Title is required")
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
    String status
  ) {
  }

  public record TrainingRegistrationAdminDto(
    UUID id,
    UUID userId,
    String userName,
    String userEmail,
    String status,
    Instant registeredAt
  ) {
  }

  public record ResourceAccessRow(
    UUID contentItemId,
    String resourceTitle,
    UUID userId,
    String userName,
    String userEmail,
    long viewCount,
    long downloadCount,
    Instant lastAccessedAt
  ) {
  }

  public record ReportsDto(
    long totalResources,
    long publishedResources,
    long pendingApprovalResources,
    long draftResources,
    long archivedResources,
    long events,
    long digitizationRecords,
    long registeredUsers,
    long filesStored,
    String totalStorage,
    long totalViews,
    long totalDownloads,
    Map<String, Long> contentByType,
    List<RecentContentDto> recentContent
  ) {
  }

  public record HeroBannerDto(
    UUID id,
    String title,
    String subtitle,
    String imageUrl,
    String linkUrl,
    int displayOrder,
    boolean active,
    Instant createdAt,
    Instant updatedAt
  ) {
  }

  public record SaveHeroBannerRequest(
    @NotBlank(message = "Title is required")
    String title,
    String subtitle,
    String linkUrl,
    Integer displayOrder,
    Boolean active
  ) {
  }
}
