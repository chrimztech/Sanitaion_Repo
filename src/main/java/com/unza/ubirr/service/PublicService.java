package com.unza.ubirr.service;

import com.unza.ubirr.dto.PublicDtos;
import com.unza.ubirr.model.AccessLevel;
import com.unza.ubirr.model.AppRole;
import com.unza.ubirr.model.AppUser;
import com.unza.ubirr.model.Category;
import com.unza.ubirr.model.ContentFile;
import com.unza.ubirr.model.ContentItem;
import com.unza.ubirr.model.ContentStatus;
import com.unza.ubirr.model.DownloadRecord;
import com.unza.ubirr.model.EventEntity;
import com.unza.ubirr.model.Review;
import com.unza.ubirr.model.ViewRecord;
import com.unza.ubirr.model.DemonstrationSite;
import com.unza.ubirr.repository.CategoryRepository;
import com.unza.ubirr.repository.ContentFileRepository;
import com.unza.ubirr.repository.ContentItemRepository;
import com.unza.ubirr.repository.DepartmentRepository;
import com.unza.ubirr.repository.DemonstrationSiteRepository;
import com.unza.ubirr.repository.DownloadRecordRepository;
import com.unza.ubirr.repository.EventRepository;
import com.unza.ubirr.repository.ReviewRepository;
import com.unza.ubirr.repository.TrainingRegistrationRepository;
import com.unza.ubirr.repository.TrainingSessionRepository;
import com.unza.ubirr.repository.SystemSettingRepository;
import com.unza.ubirr.repository.ViewRecordRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@Transactional
public class PublicService {
  // Self-injected proxy so @Cacheable is honored on calls made from within this same class
  // (Spring's cache/AOP proxy only intercepts calls that come in from other beans).
  @Lazy
  @Autowired
  private PublicService self;

  private final ContentItemRepository contentItemRepository;
  private final ContentFileRepository contentFileRepository;
  private final EventRepository eventRepository;
  private final CategoryRepository categoryRepository;
  private final DepartmentRepository departmentRepository;
  private final DemonstrationSiteRepository siteRepository;
  private final ViewRecordRepository viewRecordRepository;
  private final DownloadRecordRepository downloadRecordRepository;
  private final TrainingSessionRepository trainingSessionRepository;
  private final TrainingRegistrationRepository trainingRegistrationRepository;
  private final AuthContextService authContextService;
  private final SystemSettingRepository systemSettingRepository;
  private final ReviewRepository reviewRepository;

  public PublicService(
    ContentItemRepository contentItemRepository,
    ContentFileRepository contentFileRepository,
    EventRepository eventRepository,
    CategoryRepository categoryRepository,
    DepartmentRepository departmentRepository,
    DemonstrationSiteRepository siteRepository,
    ViewRecordRepository viewRecordRepository,
    DownloadRecordRepository downloadRecordRepository,
    TrainingSessionRepository trainingSessionRepository,
    TrainingRegistrationRepository trainingRegistrationRepository,
    AuthContextService authContextService,
    SystemSettingRepository systemSettingRepository,
    ReviewRepository reviewRepository
  ) {
    this.contentItemRepository = contentItemRepository;
    this.contentFileRepository = contentFileRepository;
    this.eventRepository = eventRepository;
    this.categoryRepository = categoryRepository;
    this.departmentRepository = departmentRepository;
    this.siteRepository = siteRepository;
    this.viewRecordRepository = viewRecordRepository;
    this.downloadRecordRepository = downloadRecordRepository;
    this.trainingSessionRepository = trainingSessionRepository;
    this.trainingRegistrationRepository = trainingRegistrationRepository;
    this.authContextService = authContextService;
    this.systemSettingRepository = systemSettingRepository;
    this.reviewRepository = reviewRepository;
  }

  @Cacheable("public-home")
  public PublicDtos.HomeResponse home() {
    List<PublicDtos.ResourceSummaryDto> resources = summarizeAll(contentItemRepository
      .findAll(publicContentSpec(null, null, null), PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "publishedAt")))
      .getContent());

    List<PublicDtos.EventDto> events = eventRepository.findAll().stream()
      .filter(event -> event.status == ContentStatus.PUBLISHED && event.accessLevel == AccessLevel.PUBLIC)
      .filter(event -> event.startDate != null && !event.startDate.isBefore(LocalDate.now()))
      .sorted(Comparator.comparing(event -> event.startDate))
      .limit(3)
      .map(this::eventDto)
      .toList();

    long totalResources = contentItemRepository.count(publicContentSpec(null, null, null));
    long activeSites = siteRepository.countByOperationalStatus("active");
    long totalSites = siteRepository.count();
    long trainingMaterials = contentItemRepository.count(publicContentSpec(null, "training_material", null));
    long research = contentItemRepository.count(publicContentSpec(null, "research_data", null))
      + contentItemRepository.count(publicContentSpec(null, "journal_article", null))
      + contentItemRepository.count(publicContentSpec(null, "thesis", null))
      + contentItemRepository.count(publicContentSpec(null, "report", null));

    PublicDtos.HubStatsDto stats = new PublicDtos.HubStatsDto(
      totalResources, totalSites, activeSites,
      eventRepository.findAll().stream().filter(e -> e.status == ContentStatus.PUBLISHED).count(),
      trainingMaterials, research
    );

    return new PublicDtos.HomeResponse(resources, events, stats);
  }

  @Cacheable("public-sites")
  public List<PublicDtos.DemonstrationSiteDto> publicSites() {
    return siteRepository.findAll(Sort.by("name")).stream()
      .filter(site -> !"decommissioned".equalsIgnoreCase(site.operationalStatus))
      .map(this::siteDto)
      .toList();
  }

  public PublicDtos.DemonstrationSiteDto siteBySlug(String slug) {
    DemonstrationSite site = siteRepository.findBySlug(slug)
      .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Site not found"));
    return siteDto(site);
  }

  private PublicDtos.DemonstrationSiteDto siteDto(DemonstrationSite s) {
    return new PublicDtos.DemonstrationSiteDto(
      s.id, s.name, s.slug, s.description, s.location, s.district,
      s.siteType, s.technology, s.operationalStatus, s.operationalSince,
      s.contactPerson, s.contactEmail, s.partnerInstitution,
      s.latitude, s.longitude, s.notes, s.createdAt,
      s.imagePath != null ? "/api/files/site/" + s.id + "/image" : null
    );
  }

  public List<PublicDtos.ResourceSummaryDto> publicResources(String query, String type, String category) {
    return publicResources(query, type, category, null);
  }

  // Cached — this is the highest-traffic read (library browse/search). Key must include the
  // logged-in flag: browse results differ (UNIVERSITY-level items are only included for
  // authenticated users), so caching without it would leak gated content to anonymous users.
  @Cacheable(value = "public-resources",
    key = "(#query == null ? '' : #query) + '|' + #type + '|' + (#category == null ? '' : #category) + '|' + #departmentId + '|' + @authContextService.isAuthenticated()")
  public List<PublicDtos.ResourceSummaryDto> publicResources(String query, String type, String category, UUID departmentId) {
    boolean loggedIn = authContextService.isAuthenticated();
    String contentType = null;
    if (type != null && !type.isBlank() && !"all".equalsIgnoreCase(type)) {
      contentType = type.trim().toLowerCase().replace('-', '_');
    }

    // Use GIN-indexed FTS with relevance ranking when a search query is present
    if (query != null && !query.isBlank()) {
      return summarizeAll(contentItemRepository.fullTextSearch(
          query,
          (category != null && !category.isBlank()) ? category : null,
          contentType,
          departmentId != null ? departmentId.toString() : null
        ));
    }

    // No query — standard JPA Specification (date-ordered browse)
    return summarizeAll(contentItemRepository.findAll(
        publicContentSpec(null, contentType, category, departmentId, loggedIn),
        PageRequest.of(0, 60, Sort.by(Sort.Direction.DESC, "publishedAt"))
      ).getContent());
  }

  @Cacheable("public-departments")
  public List<PublicDtos.DepartmentDto> departments() {
    return departmentRepository.findAll(Sort.by("name")).stream()
      .map(d -> new PublicDtos.DepartmentDto(d.id, d.name, d.code))
      .toList();
  }

  public PublicDtos.ResourceDetailDto resourceBySlug(String slug) {
    // Access check runs on every request (uncached) — only the expensive DTO assembly below
    // is cached, so a cache hit can never bypass the permission gate.
    ContentItem gate = contentItemRepository.findBySlug(slug).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resource not found"));
    if (!canAccess(gate, authContextService.currentUser().orElse(null))) {
      throw new ResponseStatusException(NOT_FOUND, "Resource not found");
    }
    return self.cachedResourceDetail(gate.id);
  }

  @Cacheable("public-resource")
  public PublicDtos.ResourceDetailDto cachedResourceDetail(UUID itemId) {
    ContentItem item = contentItemRepository.findById(itemId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resource not found"));

    List<ContentFile> files = contentFileRepository.findByContentItem_IdOrderByCreatedAtAsc(item.id);
    List<PublicDtos.ContentFileDto> fileDtos = files.stream().map(this::fileDto).toList();

    List<PublicDtos.ResourceSummaryDto> related = item.category == null
      ? List.of()
      : summarizeAll(contentItemRepository.findAll(publicContentSpec(null, null, item.category.slug), PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "publishedAt"))).getContent().stream()
        .filter(candidate -> !candidate.id.equals(item.id))
        .limit(4)
        .toList());

    PublicDtos.CategoryDto categoryDto = item.category == null
      ? null
      : new PublicDtos.CategoryDto(item.category.id, item.category.name, item.category.slug, item.category.description, 0);

    String thumbnailUrl;
    if (item.thumbnailPath != null) {
      thumbnailUrl = "/api/files/thumbnail/" + item.id;
    } else {
      thumbnailUrl = files.stream()
        .filter(f -> f.mimeType != null && f.mimeType.startsWith("image/"))
        .findFirst()
        .map(f -> "/api/files/" + f.id + "/preview")
        .orElse(null);
    }

    return new PublicDtos.ResourceDetailDto(
      item.id,
      item.title,
      item.slug,
      item.summary,
      item.body,
      item.contentType,
      item.accessLevel.apiValue(),
      item.publishedAt,
      item.downloadable,
      item.copyrightStatus,
      item.licenceInformation,
      item.language,
      item.keywords,
      item.authors,
      item.publisher,
      item.publicationDate,
      item.activityDate,
      item.sourceInstitution,
      item.externalIdentifier,
      item.bibliographicInfo,
      categoryDto,
      fileDtos,
      related,
      thumbnailUrl,
      reviewRepository.averageRating(item.id),
      reviewRepository.countByContentItem_Id(item.id)
    );
  }

  public List<PublicDtos.ReviewDto> reviewsForResource(UUID contentItemId) {
    ContentItem gate = contentItemRepository.findById(contentItemId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resource not found"));
    if (!canAccess(gate, authContextService.currentUser().orElse(null))) {
      throw new ResponseStatusException(NOT_FOUND, "Resource not found");
    }
    UUID currentUserId = authContextService.currentUserIdOrNull();
    return reviewRepository.findByContentItem_IdOrderByCreatedAtDesc(contentItemId).stream()
      .map(r -> new PublicDtos.ReviewDto(
        r.id,
        r.user.fullName,
        currentUserId != null && currentUserId.equals(r.user.id),
        r.rating,
        r.body,
        r.createdAt,
        r.updatedAt
      ))
      .toList();
  }

  @Cacheable("public-events")
  public List<PublicDtos.EventDto> publicEvents() {
    return eventRepository.findAll().stream()
      .filter(event -> event.status == ContentStatus.PUBLISHED && event.accessLevel == AccessLevel.PUBLIC)
      .sorted(Comparator.comparing(event -> event.startDate))
      .map(this::eventDto)
      .toList();
  }

  public PublicDtos.EventDto eventBySlug(String slug) {
    EventEntity event = eventRepository.findBySlug(slug).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Event not found"));
    if (event.status != ContentStatus.PUBLISHED || event.accessLevel != AccessLevel.PUBLIC) {
      throw new ResponseStatusException(NOT_FOUND, "Event not found");
    }
    return eventDto(event);
  }

  @Cacheable("public-categories")
  public List<PublicDtos.CategoryDto> categories() {
    List<Category> categories = categoryRepository.findAll(Sort.by("name"));
    Map<UUID, Long> counts = new LinkedHashMap<>();
    publicResources("", "all", null).forEach(item -> {
      if (item.categorySlug() == null) {
        return;
      }
      categoryRepository.findBySlug(item.categorySlug()).ifPresent(category -> counts.merge(category.id, 1L, Long::sum));
    });
    return categories.stream()
      .map(category -> new PublicDtos.CategoryDto(
        category.id,
        category.name,
        category.slug,
        category.description,
        counts.getOrDefault(category.id, 0L)
      ))
      .toList();
  }

  @Cacheable("public-gallery")
  public List<PublicDtos.ResourceSummaryDto> gallery() {
    return summarizeAll(contentItemRepository.findAll(publicContentSpec(null, null, null), PageRequest.of(0, 120, Sort.by(Sort.Direction.DESC, "publishedAt"))).getContent().stream()
      .filter(item -> "image".equals(item.contentType) || "gallery".equals(item.contentType))
      .toList());
  }

  public void recordView(UUID contentId, String ipAddress) {
    ContentItem item = contentItemRepository.findById(contentId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resource not found"));
    ViewRecord viewRecord = new ViewRecord();
    viewRecord.contentItem = item;
    viewRecord.user = authContextService.currentUser().orElse(null);
    viewRecord.ipAddress = ipAddress;
    viewRecordRepository.save(viewRecord);
    // Atomic UPDATE instead of read-modify-write — avoids lost increments when the same
    // popular item is viewed concurrently by many users.
    contentItemRepository.incrementViewCount(contentId);
  }

  public ContentFile fileForPreview(UUID fileId) {
    return contentFileRepository.findById(fileId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "File not found"));
  }

  public ContentFile fileForDownload(UUID fileId, String ipAddress) {
    boolean requiresLogin = systemSettingRepository.findBySettingKey("download_requires_login")
      .map(s -> "true".equalsIgnoreCase(s.settingValue))
      .orElse(true);
    if (requiresLogin && authContextService.currentUser().isEmpty()) {
      throw new ResponseStatusException(UNAUTHORIZED, "Sign in to download files");
    }
    ContentFile file = contentFileRepository.findById(fileId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "File not found"));
    DownloadRecord downloadRecord = new DownloadRecord();
    downloadRecord.contentFile = file;
    downloadRecord.user = authContextService.currentUser().orElse(null);
    downloadRecord.ipAddress = ipAddress;
    downloadRecordRepository.save(downloadRecord);
    return file;
  }

  public String buildRss(String baseUrl) {
    String normalizedBase = normalizeBaseUrl(baseUrl);
    StringBuilder items = new StringBuilder();
    contentItemRepository.findAll(publicContentSpec(null, null, null), PageRequest.of(0, 40, Sort.by(Sort.Direction.DESC, "publishedAt"))).forEach(item -> {
      items.append("""
        <item>
          <title>%s</title>
          <link>%s/resource/%s</link>
          <guid isPermaLink="true">%s/resource/%s</guid>
          <description>%s</description>
          <pubDate>%s</pubDate>
        </item>
        """.formatted(
        escapeXml(item.title),
        normalizedBase,
        item.slug,
        normalizedBase,
        item.slug,
        escapeXml(item.summary == null ? "" : item.summary),
        item.publishedAt != null ? item.publishedAt.toString() : Instant.now().toString()
      ));
    });
    return """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0">
        <channel>
          <title>UBIRR - UNZA Biosafety Repository</title>
          <link>%s</link>
          <description>Latest resources published in the University of Zambia Biosafety Information &amp; Resource Repository.</description>
          <language>en</language>
          %s
        </channel>
      </rss>
      """.formatted(normalizedBase, items);
  }

  public String buildSitemap(String baseUrl) {
    String normalizedBase = normalizeBaseUrl(baseUrl);
    List<String> entries = new ArrayList<>();
    for (String path : List.of("/", "/about", "/library", "/categories", "/gallery", "/events", "/faq", "/contact")) {
      entries.add("<url><loc>" + normalizedBase + path + "</loc><changefreq>weekly</changefreq></url>");
    }
    contentItemRepository.findAll(publicContentSpec(null, null, null)).forEach(item ->
      entries.add("<url><loc>" + normalizedBase + "/resource/" + item.slug + "</loc><lastmod>" + item.updatedAt + "</lastmod></url>")
    );
    publicEvents().forEach(event ->
      entries.add("<url><loc>" + normalizedBase + "/events/" + event.slug() + "</loc><lastmod>" + (event.publishedAt() != null ? event.publishedAt() : Instant.now()) + "</lastmod></url>")
    );
    categoryRepository.findAll(Sort.by("name")).forEach(category ->
      entries.add("<url><loc>" + normalizedBase + "/library?category=" + category.slug + "</loc></url>")
    );
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" + String.join("\n", entries) + "\n</urlset>";
  }

  public PublicDtos.ResourceSummaryDto summary(ContentItem item) {
    Map<UUID, ReviewRepository.RatingAggRow> ratings = ratingAggMap(List.of(item.id));
    return summary(item, null, ratings);
  }

  // Batches the per-item "find a fallback thumbnail" lookup into a single query instead of
  // one query per row (the previous code issued findByContentItem_Id for every item in a
  // listing that had no explicit thumbnailPath — up to ~60-120 extra queries per page load).
  private List<PublicDtos.ResourceSummaryDto> summarizeAll(List<ContentItem> items) {
    List<UUID> needsFallbackThumbnail = items.stream()
      .filter(item -> item.thumbnailPath == null)
      .map(item -> item.id)
      .toList();

    Map<UUID, String> fallbackThumbnails = needsFallbackThumbnail.isEmpty()
      ? Map.of()
      : contentFileRepository.findByContentItem_IdInOrderByCreatedAtAsc(needsFallbackThumbnail).stream()
          .filter(file -> file.mimeType != null && file.mimeType.startsWith("image/"))
          .collect(Collectors.toMap(
            file -> file.contentItem.id,
            file -> "/api/files/" + file.id + "/preview",
            (firstByCreatedAt, ignored) -> firstByCreatedAt
          ));

    Map<UUID, ReviewRepository.RatingAggRow> ratings = ratingAggMap(items.stream().map(item -> item.id).toList());

    return items.stream().map(item -> summary(item, fallbackThumbnails, ratings)).toList();
  }

  private Map<UUID, ReviewRepository.RatingAggRow> ratingAggMap(List<UUID> contentItemIds) {
    if (contentItemIds.isEmpty()) return Map.of();
    return reviewRepository.ratingSummaries(contentItemIds).stream()
      .collect(Collectors.toMap(ReviewRepository.RatingAggRow::getContentItemId, row -> row));
  }

  private PublicDtos.ResourceSummaryDto summary(ContentItem item, Map<UUID, String> fallbackThumbnails, Map<UUID, ReviewRepository.RatingAggRow> ratings) {
    String thumbnail;
    if (item.thumbnailPath != null) {
      thumbnail = "/api/files/thumbnail/" + item.id;
    } else if (fallbackThumbnails != null) {
      thumbnail = fallbackThumbnails.get(item.id);
    } else {
      List<ContentFile> files = contentFileRepository.findByContentItem_IdOrderByCreatedAtAsc(item.id);
      thumbnail = files.stream()
        .filter(file -> file.mimeType != null && file.mimeType.startsWith("image/"))
        .findFirst()
        .map(file -> "/api/files/" + file.id + "/preview")
        .orElse(null);
    }
    ReviewRepository.RatingAggRow rating = ratings == null ? null : ratings.get(item.id);
    return new PublicDtos.ResourceSummaryDto(
      item.id,
      item.title,
      item.slug,
      item.summary,
      item.contentType,
      item.accessLevel.apiValue(),
      item.publishedAt,
      thumbnail,
      item.category != null ? item.category.slug : null,
      rating != null ? rating.getAvgRating() : 0,
      rating != null ? rating.getReviewCount() : 0
    );
  }

  private PublicDtos.ContentFileDto fileDto(ContentFile file) {
    return new PublicDtos.ContentFileDto(
      file.id,
      file.originalFileName,
      file.mimeType,
      file.fileSize,
      "/api/files/" + file.id + "/preview",
      "/api/files/" + file.id + "/download"
    );
  }

  private PublicDtos.EventDto eventDto(EventEntity event) {
    return new PublicDtos.EventDto(
      event.id,
      event.title,
      event.slug,
      event.description,
      event.venue,
      event.startDate,
      event.endDate,
      event.startTime,
      event.endTime,
      event.organizer,
      event.contactEmail,
      event.registrationLink,
      event.accessLevel.apiValue(),
      event.status.apiValue(),
      event.publishedAt,
      event.bannerImagePath != null ? "/api/files/event/" + event.id + "/image" : null
    );
  }

  private Specification<ContentItem> publicContentSpec(String query, String contentType, String categorySlug) {
    return publicContentSpec(query, contentType, categorySlug, null, false);
  }

  private Specification<ContentItem> publicContentSpec(String query, String contentType, String categorySlug, UUID departmentId, boolean includeUniversity) {
    return (root, ignored, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(builder.isNull(root.get("deletedAt")));
      predicates.add(builder.equal(root.get("status"), ContentStatus.PUBLISHED));
      if (includeUniversity) {
        predicates.add(root.get("accessLevel").in(AccessLevel.PUBLIC, AccessLevel.UNIVERSITY));
      } else {
        predicates.add(builder.equal(root.get("accessLevel"), AccessLevel.PUBLIC));
      }
      if (contentType != null) {
        predicates.add(builder.equal(root.get("contentType"), contentType));
      }
      if (categorySlug != null && !categorySlug.isBlank()) {
        predicates.add(builder.equal(root.join("category").get("slug"), categorySlug));
      }
      if (departmentId != null) {
        predicates.add(builder.equal(root.join("department").get("id"), departmentId));
      }
      if (query != null && !query.isBlank()) {
        String like = "%" + query.trim().toLowerCase() + "%";
        predicates.add(builder.or(
          builder.like(builder.lower(root.get("title")), like),
          builder.like(builder.lower(root.get("summary")), like),
          builder.like(builder.lower(root.get("body")), like),
          builder.like(builder.lower(root.get("keywords")), like)
        ));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  public boolean canAccess(ContentItem item, AppUser user) {
    if (item.deletedAt != null) {
      return false;
    }
    if (item.status == ContentStatus.PUBLISHED) {
      // DRAFT access level on a published item is a data inconsistency — treat as PUBLIC
      if (item.accessLevel == AccessLevel.PUBLIC || item.accessLevel == AccessLevel.DRAFT) {
        return true;
      }
    }
    if (user == null) {
      return false;
    }
    if (authContextService.canManageContent(user)) {
      return true;
    }
    if (item.author != null && item.author.id.equals(user.id)) {
      return true;
    }
    if (item.status != ContentStatus.PUBLISHED) {
      return false;
    }
    return switch (item.accessLevel) {
      case UNIVERSITY -> true;
      case STAFF -> authContextService.hasAnyRole(user, AppRole.STAFF, AppRole.SUPER_ADMIN, AppRole.PROJECT_ADMIN);
      case STUDENT -> authContextService.hasAnyRole(user, AppRole.STUDENT, AppRole.SUPER_ADMIN, AppRole.PROJECT_ADMIN);
      case CORE_TEAM -> authContextService.canManageContent(user);
      case PUBLIC, DRAFT -> true;
    };
  }

  private String normalizeBaseUrl(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      return "";
    }
    return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  private String escapeXml(String value) {
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
  }

  public List<PublicDtos.TrainingSessionPublicDto> publicTrainingSessions() {
    return trainingSessionRepository.findByStatusIn(List.of(ContentStatus.PUBLISHED, ContentStatus.COMPLETED)).stream()
      .sorted(Comparator.comparing(s -> s.startDate))
      .map(s -> new PublicDtos.TrainingSessionPublicDto(
        s.id, s.title, s.slug, s.description, s.mode, s.venue, s.instructor,
        s.startDate, s.endDate, s.startTime, s.endTime, s.capacity,
        s.requirements, s.contactEmail, s.status.apiValue(), s.publishedAt,
        trainingRegistrationRepository.countActiveBySessionId(s.id),
        s.imagePath != null ? "/api/files/training/" + s.id + "/image" : null
      ))
      .toList();
  }
}
