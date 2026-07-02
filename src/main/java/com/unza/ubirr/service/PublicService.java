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
import com.unza.ubirr.model.ViewRecord;
import com.unza.ubirr.model.DemonstrationSite;
import com.unza.ubirr.model.TrainingSession;
import com.unza.ubirr.repository.CategoryRepository;
import com.unza.ubirr.repository.ContentFileRepository;
import com.unza.ubirr.repository.ContentItemRepository;
import com.unza.ubirr.repository.DepartmentRepository;
import com.unza.ubirr.repository.DemonstrationSiteRepository;
import com.unza.ubirr.repository.DownloadRecordRepository;
import com.unza.ubirr.repository.EventRepository;
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
import org.springframework.cache.annotation.Cacheable;
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
    SystemSettingRepository systemSettingRepository
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
  }

  @Cacheable("public-home")
  public PublicDtos.HomeResponse home() {
    List<PublicDtos.ResourceSummaryDto> resources = contentItemRepository
      .findAll(publicContentSpec(null, null, null), PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "publishedAt")))
      .getContent().stream().map(this::summary).toList();

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
      s.latitude, s.longitude, s.notes, s.createdAt
    );
  }

  public List<PublicDtos.ResourceSummaryDto> publicResources(String query, String type, String category) {
    return publicResources(query, type, category, null);
  }

  @Cacheable("public-resources")
  public List<PublicDtos.ResourceSummaryDto> publicResources(String query, String type, String category, UUID departmentId) {
    String contentType = null;
    if (type != null && !type.isBlank() && !"all".equalsIgnoreCase(type)) {
      contentType = type.trim().toLowerCase().replace('-', '_');
    }

    // Use GIN-indexed FTS with relevance ranking when a search query is present
    if (query != null && !query.isBlank()) {
      return contentItemRepository.fullTextSearch(
          query,
          (category != null && !category.isBlank()) ? category : null,
          contentType,
          departmentId != null ? departmentId.toString() : null
        ).stream().map(this::summary).toList();
    }

    // No query — standard JPA Specification (date-ordered browse)
    return contentItemRepository.findAll(
        publicContentSpec(null, contentType, category, departmentId),
        PageRequest.of(0, 60, Sort.by(Sort.Direction.DESC, "publishedAt"))
      ).getContent().stream().map(this::summary).toList();
  }

  @Cacheable("public-departments")
  public List<PublicDtos.DepartmentDto> departments() {
    return departmentRepository.findAll(Sort.by("name")).stream()
      .map(d -> new PublicDtos.DepartmentDto(d.id, d.name, d.code))
      .toList();
  }

  @Cacheable(value = "public-resource", key = "#slug")
  public PublicDtos.ResourceDetailDto resourceBySlug(String slug) {
    ContentItem item = contentItemRepository.findBySlug(slug).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resource not found"));
    if (!canAccess(item, authContextService.currentUser().orElse(null))) {
      throw new ResponseStatusException(NOT_FOUND, "Resource not found");
    }

    List<ContentFile> files = contentFileRepository.findByContentItem_IdOrderByCreatedAtAsc(item.id);
    List<PublicDtos.ContentFileDto> fileDtos = files.stream().map(this::fileDto).toList();

    List<PublicDtos.ResourceSummaryDto> related = item.category == null
      ? List.of()
      : contentItemRepository.findAll(publicContentSpec(null, null, item.category.slug), PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "publishedAt"))).getContent().stream()
        .filter(candidate -> !candidate.id.equals(item.id))
        .limit(4)
        .map(this::summary)
        .toList();

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
      thumbnailUrl
    );
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
    return contentItemRepository.findAll(publicContentSpec(null, null, null), PageRequest.of(0, 120, Sort.by(Sort.Direction.DESC, "publishedAt"))).getContent().stream()
      .filter(item -> "image".equals(item.contentType) || "gallery".equals(item.contentType))
      .map(this::summary)
      .toList();
  }

  public void recordView(UUID contentId, String ipAddress) {
    ContentItem item = contentItemRepository.findById(contentId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resource not found"));
    if (!canAccess(item, authContextService.currentUser().orElse(null))) {
      throw new ResponseStatusException(NOT_FOUND, "Resource not found");
    }
    ViewRecord viewRecord = new ViewRecord();
    viewRecord.contentItem = item;
    viewRecord.user = authContextService.currentUser().orElse(null);
    viewRecord.ipAddress = ipAddress;
    viewRecordRepository.save(viewRecord);
    item.viewCount = item.viewCount + 1;
    contentItemRepository.save(item);
  }

  public ContentFile fileForPreview(UUID fileId) {
    ContentFile file = contentFileRepository.findById(fileId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "File not found"));
    if (!canAccess(file.contentItem, authContextService.currentUser().orElse(null))) {
      throw new ResponseStatusException(NOT_FOUND, "File not found");
    }
    return file;
  }

  public ContentFile fileForDownload(UUID fileId, String ipAddress) {
    boolean requiresLogin = systemSettingRepository.findBySettingKey("download_requires_login")
      .map(s -> "true".equalsIgnoreCase(s.settingValue))
      .orElse(true);
    if (requiresLogin && authContextService.currentUser().isEmpty()) {
      throw new ResponseStatusException(UNAUTHORIZED, "Sign in to download files");
    }
    ContentFile file = fileForPreview(fileId);
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
    String thumbnail;
    if (item.thumbnailPath != null) {
      thumbnail = "/api/files/thumbnail/" + item.id;
    } else {
      List<ContentFile> files = contentFileRepository.findByContentItem_IdOrderByCreatedAtAsc(item.id);
      thumbnail = files.stream()
        .filter(file -> file.mimeType != null && file.mimeType.startsWith("image/"))
        .findFirst()
        .map(file -> "/api/files/" + file.id + "/preview")
        .orElse(null);
    }
    return new PublicDtos.ResourceSummaryDto(
      item.id,
      item.title,
      item.slug,
      item.summary,
      item.contentType,
      item.accessLevel.apiValue(),
      item.publishedAt,
      thumbnail,
      item.category != null ? item.category.slug : null
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
      event.publishedAt
    );
  }

  private Specification<ContentItem> publicContentSpec(String query, String contentType, String categorySlug) {
    return publicContentSpec(query, contentType, categorySlug, null);
  }

  private Specification<ContentItem> publicContentSpec(String query, String contentType, String categorySlug, UUID departmentId) {
    return (root, ignored, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(builder.isNull(root.get("deletedAt")));
      predicates.add(builder.equal(root.get("status"), ContentStatus.PUBLISHED));
      predicates.add(builder.equal(root.get("accessLevel"), AccessLevel.PUBLIC));
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
      return builder.and(predicates.toArray(Predicate[]::new));
    };
  }

  public boolean canAccess(ContentItem item, AppUser user) {
    if (item.deletedAt != null) {
      return false;
    }
    if (item.status == ContentStatus.PUBLISHED && item.accessLevel == AccessLevel.PUBLIC) {
      return true;
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
      case PUBLIC -> true;
      case DRAFT -> false;
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
        trainingRegistrationRepository.countActiveBySessionId(s.id)
      ))
      .toList();
  }
}
