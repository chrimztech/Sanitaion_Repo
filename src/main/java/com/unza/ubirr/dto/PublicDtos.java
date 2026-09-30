package com.unza.ubirr.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class PublicDtos {
  private PublicDtos() {
  }

  public record CategoryDto(
    UUID id,
    String name,
    String slug,
    String description,
    long count
  ) {
  }

  public record ContentFileDto(
    UUID id,
    String originalFileName,
    String mimeType,
    Long fileSize,
    String previewUrl,
    String downloadUrl
  ) {
  }

  public record ResourceSummaryDto(
    UUID id,
    String title,
    String slug,
    String summary,
    String contentType,
    String accessLevel,
    Instant publishedAt,
    String thumbnailUrl,
    String categorySlug
  ) {
  }

  public record ResourceDetailDto(
    UUID id,
    String title,
    String slug,
    String summary,
    String body,
    String contentType,
    String accessLevel,
    Instant publishedAt,
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
    CategoryDto category,
    List<ContentFileDto> files,
    List<ResourceSummaryDto> relatedResources,
    String thumbnailUrl
  ) {
  }

  public record EventDto(
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

  public record DemonstrationSiteDto(
    java.util.UUID id,
    String name,
    String slug,
    String description,
    String location,
    String district,
    String siteType,
    String technology,
    String operationalStatus,
    java.time.LocalDate operationalSince,
    String contactPerson,
    String contactEmail,
    String partnerInstitution,
    Double latitude,
    Double longitude,
    String notes,
    java.time.Instant createdAt,
    String imageUrl
  ) {
  }

  public record HubStatsDto(
    long totalResources,
    long demonstrationSites,
    long activeSites,
    long publishedEvents,
    long trainingMaterials,
    long researchOutputs
  ) {
  }

  public record HomeResponse(
    List<ResourceSummaryDto> resources,
    List<EventDto> events,
    HubStatsDto stats
  ) {
  }

  public record DepartmentDto(
    UUID id,
    String name,
    String code
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
    String imageUrl
  ) {
  }
}
