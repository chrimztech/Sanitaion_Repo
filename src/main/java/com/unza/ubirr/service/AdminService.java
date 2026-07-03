package com.unza.ubirr.service;

import com.unza.ubirr.dto.AdminDtos;
import com.unza.ubirr.dto.PublicDtos;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import com.unza.ubirr.model.AccessLevel;
import com.unza.ubirr.model.AppRole;
import com.unza.ubirr.model.AppUser;
import com.unza.ubirr.model.ApprovalWorkflow;
import com.unza.ubirr.model.Category;
import com.unza.ubirr.model.ContentFile;
import com.unza.ubirr.model.ContentItem;
import com.unza.ubirr.model.ContentStatus;
import com.unza.ubirr.model.DemonstrationSite;
import com.unza.ubirr.model.Department;
import com.unza.ubirr.model.DigitizationRecord;
import com.unza.ubirr.model.DigitizationStatus;
import com.unza.ubirr.model.EventEntity;
import com.unza.ubirr.model.Notification;
import com.unza.ubirr.model.School;
import com.unza.ubirr.model.SystemSetting;
import com.unza.ubirr.model.Tag;
import com.unza.ubirr.model.AuthType;
import com.unza.ubirr.model.TrainingRegistration;
import com.unza.ubirr.model.TrainingSession;
import com.unza.ubirr.model.UserRoleAssignment;
import com.unza.ubirr.model.UserSession;
import com.unza.ubirr.repository.AppUserRepository;
import com.unza.ubirr.repository.TrainingRegistrationRepository;
import com.unza.ubirr.repository.TrainingSessionRepository;
import com.unza.ubirr.repository.ApprovalWorkflowRepository;
import com.unza.ubirr.repository.AuditLogRepository;
import com.unza.ubirr.repository.BookmarkRepository;
import com.unza.ubirr.model.ContentTypeDef;
import com.unza.ubirr.repository.ContentTypeDefRepository;
import com.unza.ubirr.repository.DemonstrationSiteRepository;
import com.unza.ubirr.repository.CategoryRepository;
import com.unza.ubirr.repository.ContentFileRepository;
import com.unza.ubirr.repository.ContentItemRepository;
import com.unza.ubirr.repository.DepartmentRepository;
import com.unza.ubirr.repository.DigitizationRecordRepository;
import com.unza.ubirr.repository.DownloadRecordRepository;
import com.unza.ubirr.repository.EventRepository;
import com.unza.ubirr.repository.NotificationRepository;
import com.unza.ubirr.repository.ReadingListItemRepository;
import com.unza.ubirr.repository.ReadingListRepository;
import com.unza.ubirr.repository.SchoolRepository;
import com.unza.ubirr.repository.SystemSettingRepository;
import com.unza.ubirr.repository.TagRepository;
import com.unza.ubirr.repository.UserRoleRepository;
import com.unza.ubirr.repository.UserSessionRepository;
import com.unza.ubirr.repository.ViewRecordRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class AdminService {
  private final AuthContextService authContextService;
  private final ContentItemRepository contentItemRepository;
  private final ContentFileRepository contentFileRepository;
  private final ApprovalWorkflowRepository approvalWorkflowRepository;
  private final EventRepository eventRepository;
  private final DigitizationRecordRepository digitizationRecordRepository;
  private final CategoryRepository categoryRepository;
  private final DepartmentRepository departmentRepository;
  private final SchoolRepository schoolRepository;
  private final TagRepository tagRepository;
  private final AppUserRepository appUserRepository;
  private final UserRoleRepository userRoleRepository;
  private final AuditLogRepository auditLogRepository;
  private final SystemSettingRepository systemSettingRepository;
  private final NotificationRepository notificationRepository;
  private final ViewRecordRepository viewRecordRepository;
  private final DownloadRecordRepository downloadRecordRepository;
  private final BookmarkRepository bookmarkRepository;
  private final ReadingListRepository readingListRepository;
  private final ReadingListItemRepository readingListItemRepository;
  private final DemonstrationSiteRepository demonstrationSiteRepository;
  private final ContentTypeDefRepository contentTypeDefRepository;
  private final TrainingSessionRepository trainingSessionRepository;
  private final TrainingRegistrationRepository trainingRegistrationRepository;
  private final UserSessionRepository userSessionRepository;
  private final PasswordEncoder passwordEncoder;
  private final StorageService storageService;
  private final ThumbnailService thumbnailService;
  private final AuditService auditService;
  private final PublicService publicService;

  public AdminService(
    AuthContextService authContextService,
    ContentItemRepository contentItemRepository,
    ContentFileRepository contentFileRepository,
    ApprovalWorkflowRepository approvalWorkflowRepository,
    EventRepository eventRepository,
    DigitizationRecordRepository digitizationRecordRepository,
    CategoryRepository categoryRepository,
    DepartmentRepository departmentRepository,
    SchoolRepository schoolRepository,
    TagRepository tagRepository,
    AppUserRepository appUserRepository,
    UserRoleRepository userRoleRepository,
    AuditLogRepository auditLogRepository,
    SystemSettingRepository systemSettingRepository,
    NotificationRepository notificationRepository,
    ViewRecordRepository viewRecordRepository,
    DownloadRecordRepository downloadRecordRepository,
    BookmarkRepository bookmarkRepository,
    ReadingListRepository readingListRepository,
    ReadingListItemRepository readingListItemRepository,
    DemonstrationSiteRepository demonstrationSiteRepository,
    ContentTypeDefRepository contentTypeDefRepository,
    TrainingSessionRepository trainingSessionRepository,
    TrainingRegistrationRepository trainingRegistrationRepository,
    UserSessionRepository userSessionRepository,
    PasswordEncoder passwordEncoder,
    StorageService storageService,
    ThumbnailService thumbnailService,
    AuditService auditService,
    PublicService publicService
  ) {
    this.authContextService = authContextService;
    this.contentItemRepository = contentItemRepository;
    this.contentFileRepository = contentFileRepository;
    this.approvalWorkflowRepository = approvalWorkflowRepository;
    this.eventRepository = eventRepository;
    this.digitizationRecordRepository = digitizationRecordRepository;
    this.categoryRepository = categoryRepository;
    this.departmentRepository = departmentRepository;
    this.schoolRepository = schoolRepository;
    this.tagRepository = tagRepository;
    this.appUserRepository = appUserRepository;
    this.userRoleRepository = userRoleRepository;
    this.auditLogRepository = auditLogRepository;
    this.systemSettingRepository = systemSettingRepository;
    this.notificationRepository = notificationRepository;
    this.viewRecordRepository = viewRecordRepository;
    this.downloadRecordRepository = downloadRecordRepository;
    this.bookmarkRepository = bookmarkRepository;
    this.readingListRepository = readingListRepository;
    this.readingListItemRepository = readingListItemRepository;
    this.demonstrationSiteRepository = demonstrationSiteRepository;
    this.contentTypeDefRepository = contentTypeDefRepository;
    this.trainingSessionRepository = trainingSessionRepository;
    this.trainingRegistrationRepository = trainingRegistrationRepository;
    this.userSessionRepository = userSessionRepository;
    this.passwordEncoder = passwordEncoder;
    this.storageService = storageService;
    this.thumbnailService = thumbnailService;
    this.auditService = auditService;
    this.publicService = publicService;
  }

  public AdminDtos.DashboardSummaryDto dashboard() {
    authContextService.requireContentManager();
    List<ContentItem> items = contentItemRepository.findAll();
    List<EventEntity> events = eventRepository.findAll();
    List<DigitizationRecord> digitization = digitizationRecordRepository.findAll();
    List<AdminDtos.RecentContentDto> recent = items.stream()
      .sorted(Comparator.comparing((ContentItem item) -> item.updatedAt).reversed())
      .limit(8)
      .map(this::recentContent)
      .toList();
    return new AdminDtos.DashboardSummaryDto(
      items.size(),
      items.stream().filter(item -> item.status == ContentStatus.PUBLISHED).count(),
      items.stream().filter(item -> item.status == ContentStatus.DRAFT).count(),
      items.stream().filter(item -> item.status == ContentStatus.SUBMITTED).count(),
      events.stream().filter(event -> event.status == ContentStatus.PUBLISHED).count(),
      digitization.size(),
      recent
    );
  }

  public List<AdminDtos.ContentEditorDto> adminContent(String status) {
    authContextService.requireContentManager();
    return contentItemRepository.findAllForAdmin().stream()
      .filter(item -> status == null || status.isBlank() || "all".equalsIgnoreCase(status) || item.status == EnumMapper.status(status))
      .map(this::contentEditorDto)
      .toList();
  }

  @Caching(evict = {
    @CacheEvict(value = "public-resources", allEntries = true),
    @CacheEvict(value = "public-resource",  allEntries = true),
    @CacheEvict(value = "public-home",      allEntries = true),
    @CacheEvict(value = "public-categories", allEntries = true),
    @CacheEvict(value = "public-gallery",   allEntries = true),
  })
  public AdminDtos.ContentEditorDto saveContent(UUID id, AdminDtos.SaveContentRequest request) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    ContentItem item = id == null
      ? new ContentItem()
      : contentItemRepository.findByIdForAdmin(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Content not found"));

    item.title = request.title().trim();
    item.slug = uniqueSlug(id, request.slug(), request.title(), false);
    item.summary = request.summary();
    item.body = request.body();
    item.contentType = request.contentType() != null && !request.contentType().isBlank()
      ? request.contentType().trim().toLowerCase().replace('-', '_')
      : "article";
    item.accessLevel = EnumMapper.access(request.accessLevel());
    item.status = request.status() == null || request.status().isBlank() ? item.status : EnumMapper.status(request.status());
    item.downloadable = request.downloadable() == null || request.downloadable();
    item.copyrightStatus = request.copyrightStatus();
    item.licenceInformation = request.licenceInformation();
    item.language = request.language() == null || request.language().isBlank() ? "en" : request.language();
    item.keywords = request.keywords();
    item.authors = request.authors();
    item.publisher = request.publisher();
    item.publicationDate = request.publicationDate();
    item.activityDate = request.activityDate();
    item.sourceInstitution = request.sourceInstitution();
    item.externalIdentifier = request.externalIdentifier();
    item.bibliographicInfo = request.bibliographicInfo();
    item.author = item.author == null ? user : item.author;
    item.category = nullableCategory(request.categoryId());
    item.department = nullableDepartment(request.departmentId());
    if (item.status == ContentStatus.PUBLISHED && item.publishedAt == null) {
      item.publishedAt = java.time.Instant.now();
    }
    ContentItem saved = contentItemRepository.save(item);
    auditService.log(user, id == null ? "content.create" : "content.update", "content_items", saved.id.toString());
    return contentEditorDto(saved);
  }

  @Caching(evict = {
    @CacheEvict(value = "public-resources", allEntries = true),
    @CacheEvict(value = "public-resource",  allEntries = true),
    @CacheEvict(value = "public-home",      allEntries = true),
  })
  public void transitionContent(UUID id, AdminDtos.ContentTransitionRequest request) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    ContentItem item = contentItemRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Content not found"));
    ContentStatus nextStatus = switch (request.action().trim().toLowerCase()) {
      case "submit" -> ContentStatus.SUBMITTED;
      case "approve", "approved" -> ContentStatus.APPROVED;
      case "return", "returned" -> ContentStatus.RETURNED;
      case "reject", "rejected" -> ContentStatus.REJECTED;
      case "publish", "published" -> {
        if (!authContextService.isAdmin(user)) {
          throw new ResponseStatusException(BAD_REQUEST, "Only administrators can publish content");
        }
        yield ContentStatus.PUBLISHED;
      }
      case "archive", "archived" -> ContentStatus.ARCHIVED;
      default -> throw new ResponseStatusException(BAD_REQUEST, "Unsupported action");
    };

    item.status = nextStatus;
    if (nextStatus == ContentStatus.PUBLISHED) {
      item.publishedAt = java.time.Instant.now();
      // Publishing with DRAFT access level makes files inaccessible to users; default to PUBLIC
      if (item.accessLevel == AccessLevel.DRAFT) {
        item.accessLevel = AccessLevel.PUBLIC;
      }
    }
    contentItemRepository.save(item);

    ApprovalWorkflow workflow = new ApprovalWorkflow();
    workflow.contentItem = item;
    workflow.submittedBy = user;
    workflow.reviewedBy = nextStatus == ContentStatus.SUBMITTED ? null : user;
    workflow.status = nextStatus;
    workflow.reviewerComments = request.comments();
    workflow.submittedAt = java.time.Instant.now();
    workflow.reviewedAt = nextStatus == ContentStatus.SUBMITTED ? null : java.time.Instant.now();
    workflow.publishedBy = nextStatus == ContentStatus.PUBLISHED ? user : null;
    workflow.publishedAt = nextStatus == ContentStatus.PUBLISHED ? java.time.Instant.now() : null;
    approvalWorkflowRepository.save(workflow);

    if (item.author != null && !item.author.id.equals(user.id)) {
      Notification notification = new Notification();
      notification.user = item.author;
      notification.title = "Content status updated";
      notification.message = item.title + " is now marked as " + nextStatus.apiValue() + ".";
      notification.link = "/admin/content";
      notificationRepository.save(notification);
    }

    auditService.log(user, "content." + request.action().trim().toLowerCase(), "content_items", item.id.toString());
  }

  public List<PublicDtos.ContentFileDto> contentFiles(UUID contentId) {
    authContextService.requireContentManager();
    return contentFileRepository.findByContentItem_IdOrderByCreatedAtAsc(contentId).stream()
      .map(file -> new PublicDtos.ContentFileDto(
        file.id,
        file.originalFileName,
        file.mimeType,
        file.fileSize,
        "/api/files/" + file.id + "/preview",
        "/api/files/" + file.id + "/download"
      ))
      .toList();
  }

  public PublicDtos.ContentFileDto uploadContentFile(UUID contentId, MultipartFile multipartFile) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    ContentItem item = contentItemRepository.findById(contentId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Content not found"));
    StorageService.StoredFile storedFile = storageService.storeContentFile(contentId, multipartFile);

    ContentFile file = new ContentFile();
    file.contentItem = item;
    file.originalFileName = multipartFile.getOriginalFilename() != null ? multipartFile.getOriginalFilename() : "upload";
    file.storedFileName = storedFile.storedFileName();
    file.filePath = storedFile.relativePath();
    file.fileType = storedFile.mimeType() != null && storedFile.mimeType().contains("/") ? storedFile.mimeType().split("/")[0] : null;
    file.mimeType = storedFile.mimeType();
    file.fileExtension = storedFile.extension();
    file.fileSize = storedFile.size();
    file.checksum = storedFile.checksum();
    file.uploadedBy = user;
    file.accessLevel = item.accessLevel;
    file = contentFileRepository.save(file);

    // Auto-generate thumbnail if this item doesn't have one yet
    if (item.thumbnailPath == null) {
      try {
        thumbnailService.generate(
          storageService.load(storedFile.relativePath()).getFile().toPath(),
          storedFile.mimeType(),
          contentId
        ).ifPresent(thumbRelPath -> {
          item.thumbnailPath = thumbRelPath;
          contentItemRepository.save(item);
        });
      } catch (Exception ignored) {
        // Thumbnail is non-critical; continue if it fails
      }
    }

    auditService.log(user, "content.file.upload", "content_files", file.id.toString());
    return new PublicDtos.ContentFileDto(file.id, file.originalFileName, file.mimeType, file.fileSize, "/api/files/" + file.id + "/preview", "/api/files/" + file.id + "/download");
  }

  public AdminDtos.DigitizationDto uploadScanFile(UUID recordId, MultipartFile multipartFile) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    DigitizationRecord record = digitizationRecordRepository.findById(recordId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Digitization record not found"));
    StorageService.StoredFile stored = storageService.storeDigitizationFile(recordId, multipartFile);
    record.scannedFilePath = stored.relativePath();
    if (record.digitizationStatus == DigitizationStatus.PENDING
        || record.digitizationStatus == DigitizationStatus.SCANNING) {
      record.digitizationStatus = DigitizationStatus.SCANNED;
    }
    record.scannedBy = user;
    DigitizationRecord saved = digitizationRecordRepository.save(record);
    auditService.log(user, "digitization.scan.upload", "digitization_records", saved.id.toString());
    return digitizationDto(saved);
  }

  public void deleteContentFile(UUID fileId) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    ContentFile file = contentFileRepository.findById(fileId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "File not found"));
    storageService.delete(file.filePath);
    contentFileRepository.delete(file);
    auditService.log(user, "content.file.delete", "content_files", fileId.toString());
  }

  public int fixDraftAccessLevels() {
    authContextService.requireAdmin();
    AppUser user = authContextService.requireUser();
    List<ContentItem> broken = contentItemRepository.findAll().stream()
        .filter(i -> i.status == ContentStatus.PUBLISHED && i.accessLevel == AccessLevel.DRAFT)
        .toList();
    for (ContentItem i : broken) {
      i.accessLevel = AccessLevel.PUBLIC;
    }
    contentItemRepository.saveAll(broken);
    if (!broken.isEmpty()) {
      auditService.log(user, "content.fix_access_levels", "content_items", broken.size() + " items upgraded");
    }
    return broken.size();
  }

  public void deleteContent(UUID id) {
    authContextService.requireAdmin();
    AppUser user = authContextService.requireUser();
    ContentItem item = contentItemRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Content not found"));
    if (item.status != ContentStatus.DRAFT) {
      throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Only draft content can be deleted. Archive published resources instead.");
    }
    List<ContentFile> files = contentFileRepository.findByContentItem_IdOrderByCreatedAtAsc(id);
    for (ContentFile f : files) {
      storageService.delete(f.filePath);
    }
    contentFileRepository.deleteAll(files);
    contentItemRepository.delete(item);
    auditService.log(user, "content.delete", "content_items", id.toString());
  }

  public List<AdminDtos.ApprovalItemDto> approvals() {
    authContextService.requireContentManager();
    return contentItemRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
      .filter(item -> List.of(ContentStatus.SUBMITTED, ContentStatus.APPROVED, ContentStatus.RETURNED).contains(item.status))
      .map(item -> new AdminDtos.ApprovalItemDto(item.id, item.title, item.slug, item.contentType, item.status.apiValue(), item.updatedAt, item.summary))
      .toList();
  }

  public List<AdminDtos.EventAdminDto> events() {
    authContextService.requireContentManager();
    return eventRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")).stream().map(this::eventAdminDto).toList();
  }

  public AdminDtos.EventAdminDto saveEvent(UUID id, AdminDtos.SaveEventRequest request) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    EventEntity event = id == null
      ? new EventEntity()
      : eventRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Event not found"));
    event.title = request.title().trim();
    event.slug = uniqueSlug(id, request.slug(), request.title(), true);
    event.description = request.description();
    event.venue = request.venue();
    event.startDate = request.startDate();
    event.endDate = request.endDate();
    event.startTime = request.startTime();
    event.endTime = request.endTime();
    event.organizer = request.organizer();
    event.contactEmail = request.contactEmail();
    event.registrationLink = request.registrationLink();
    event.accessLevel = request.accessLevel() == null || request.accessLevel().isBlank() ? AccessLevel.PUBLIC : EnumMapper.access(request.accessLevel());
    event.status = request.status() == null || request.status().isBlank() ? ContentStatus.DRAFT : EnumMapper.status(request.status());
    event.createdBy = event.createdBy == null ? user : event.createdBy;
    if (event.status == ContentStatus.PUBLISHED && event.publishedAt == null) {
      event.publishedAt = java.time.Instant.now();
    }
    EventEntity saved = eventRepository.save(event);
    auditService.log(user, id == null ? "event.create" : "event.update", "events", saved.id.toString());
    return eventAdminDto(saved);
  }

  @CacheEvict(value = "public-events", allEntries = true)
  public void publishEvent(UUID id) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    EventEntity event = eventRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Event not found"));
    event.status = ContentStatus.PUBLISHED;
    event.accessLevel = AccessLevel.PUBLIC;
    event.publishedAt = java.time.Instant.now();
    eventRepository.save(event);
    auditService.log(user, "event.publish", "events", event.id.toString());
  }

  public void deleteEvent(UUID id) {
    authContextService.requireAdmin();
    AppUser user = authContextService.requireUser();
    EventEntity event = eventRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Event not found"));
    eventRepository.delete(event);
    auditService.log(user, "event.delete", "events", id.toString());
  }

  public List<AdminDtos.DigitizationDto> digitization() {
    authContextService.requireContentManager();
    return digitizationRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream().map(this::digitizationDto).toList();
  }

  public AdminDtos.DigitizationDto saveDigitization(UUID id, AdminDtos.SaveDigitizationRequest request) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    DigitizationRecord record = id == null
      ? new DigitizationRecord()
      : digitizationRecordRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Digitization record not found"));
    record.title = request.title().trim();
    record.description = request.description();
    record.sourceOwner = request.sourceOwner();
    record.physicalLocation = request.physicalLocation();
    record.numberOfPages = request.numberOfPages();
    record.digitizationStatus = EnumMapper.digitizationStatus(request.digitizationStatus());
    record.scannedFilePath = request.scannedFilePath();
    record.ocrText = request.ocrText();
    record.qualityStatus = request.qualityStatus();
    record.copyrightStatus = request.copyrightStatus();
    record.contentItem = request.contentItemId() == null ? null : contentItemRepository.findById(request.contentItemId()).orElse(null);
    record.scannedBy = record.scannedBy == null ? user : record.scannedBy;
    DigitizationRecord saved = digitizationRecordRepository.save(record);
    auditService.log(user, id == null ? "digitization.create" : "digitization.update", "digitization_records", saved.id.toString());
    return digitizationDto(saved);
  }

  public void deleteDigitization(UUID id) {
    authContextService.requireAdmin();
    AppUser user = authContextService.requireUser();
    DigitizationRecord record = digitizationRecordRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Digitization record not found"));
    if (record.scannedFilePath != null) {
      try { storageService.delete(record.scannedFilePath); } catch (Exception ignored) {}
    }
    digitizationRecordRepository.delete(record);
    auditService.log(user, "digitization.delete", "digitization_records", id.toString());
  }

  public List<AdminDtos.UserAdminDto> users() {
    authContextService.requireAdmin();
    return appUserRepository.findAllWithRoles().stream()
      .map(user -> new AdminDtos.UserAdminDto(
        user.id,
        user.email,
        user.fullName,
        user.status,
        user.authType != null ? user.authType.name().toLowerCase() : "local",
        user.createdAt,
        user.lastLoginAt,
        user.roles.stream().map(role -> role.role.apiValue()).toList()
      ))
      .toList();
  }

  public void updateUserRoles(UUID userId, AdminDtos.UpdateUserRolesRequest request) {
    authContextService.requireAdmin();
    AppUser actor = authContextService.requireUser();
    AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    List<UserRoleAssignment> existing = userRoleRepository.findByUser_Id(userId);
    userRoleRepository.deleteAll(existing);
    List<String> requestedRoles = request.roles() == null || request.roles().isEmpty() ? List.of("student") : request.roles();
    for (String role : requestedRoles) {
      UserRoleAssignment assignment = new UserRoleAssignment();
      assignment.user = user;
      assignment.role = EnumMapper.role(role);
      userRoleRepository.save(assignment);
    }
    auditService.log(actor, "user.roles.update", "users", userId.toString());
  }

  public List<AdminDtos.TaxonomyEntryDto> taxonomy(String type) {
    authContextService.requireAdmin();
    return switch (type.toLowerCase()) {
      case "categories" -> categoryRepository.findAll(Sort.by("name")).stream()
        .map(category -> new AdminDtos.TaxonomyEntryDto(category.id, category.name, category.slug, category.description, null, null))
        .toList();
      case "departments" -> departmentRepository.findAll(Sort.by("name")).stream()
        .map(department -> new AdminDtos.TaxonomyEntryDto(department.id, department.name, null, null, department.code, department.school != null ? department.school.id : null))
        .toList();
      case "schools" -> schoolRepository.findAll(Sort.by("name")).stream()
        .map(school -> new AdminDtos.TaxonomyEntryDto(school.id, school.name, null, null, null, null))
        .toList();
      case "tags" -> tagRepository.findAll(Sort.by("name")).stream()
        .map(tag -> new AdminDtos.TaxonomyEntryDto(tag.id, tag.name, tag.slug, null, null, null))
        .toList();
      case "content_types" -> contentTypeDefRepository.findAllByOrderByNameAsc().stream()
        .map(ct -> new AdminDtos.TaxonomyEntryDto(ct.id, ct.name, ct.slug, ct.description, null, null))
        .toList();
      default -> throw new ResponseStatusException(BAD_REQUEST, "Unsupported taxonomy type");
    };
  }

  public AdminDtos.TaxonomyEntryDto saveTaxonomy(String type, AdminDtos.SaveTaxonomyEntryRequest request) {
    authContextService.requireAdmin();
    AppUser user = authContextService.requireUser();
    return switch (type.toLowerCase()) {
      case "categories" -> {
        Category category = new Category();
        category.name = request.name().trim();
        category.slug = request.slug() == null || request.slug().isBlank() ? EnumMapper.slugify(request.name()) : EnumMapper.slugify(request.slug());
        category.description = request.description();
        category = categoryRepository.save(category);
        auditService.log(user, "taxonomy.category.create", "categories", category.id.toString());
        yield new AdminDtos.TaxonomyEntryDto(category.id, category.name, category.slug, category.description, null, null);
      }
      case "departments" -> {
        Department department = new Department();
        department.name = request.name().trim();
        department.code = request.code();
        department.school = request.schoolId() == null ? null : schoolRepository.findById(request.schoolId()).orElse(null);
        department = departmentRepository.save(department);
        auditService.log(user, "taxonomy.department.create", "departments", department.id.toString());
        yield new AdminDtos.TaxonomyEntryDto(department.id, department.name, null, null, department.code, department.school != null ? department.school.id : null);
      }
      case "schools" -> {
        School school = new School();
        school.name = request.name().trim();
        school = schoolRepository.save(school);
        auditService.log(user, "taxonomy.school.create", "schools", school.id.toString());
        yield new AdminDtos.TaxonomyEntryDto(school.id, school.name, null, null, null, null);
      }
      case "tags" -> {
        Tag tag = new Tag();
        tag.name = request.name().trim();
        tag.slug = request.slug() == null || request.slug().isBlank() ? EnumMapper.slugify(request.name()) : EnumMapper.slugify(request.slug());
        tag = tagRepository.save(tag);
        auditService.log(user, "taxonomy.tag.create", "tags", tag.id.toString());
        yield new AdminDtos.TaxonomyEntryDto(tag.id, tag.name, tag.slug, null, null, null);
      }
      case "content_types" -> {
        ContentTypeDef ct = new ContentTypeDef();
        ct.name = request.name().trim();
        ct.slug = request.slug() == null || request.slug().isBlank()
          ? EnumMapper.slugify(request.name()).replace('-', '_')
          : request.slug().trim().toLowerCase().replace('-', '_').replace(' ', '_');
        ct.description = request.description();
        ct = contentTypeDefRepository.save(ct);
        auditService.log(user, "taxonomy.content_type.create", "content_type_defs", ct.id.toString());
        yield new AdminDtos.TaxonomyEntryDto(ct.id, ct.name, ct.slug, ct.description, null, null);
      }
      default -> throw new ResponseStatusException(BAD_REQUEST, "Unsupported taxonomy type");
    };
  }

  public AdminDtos.TaxonomyEntryDto updateTaxonomy(String type, UUID id, AdminDtos.SaveTaxonomyEntryRequest request) {
    authContextService.requireAdmin();
    return switch (type.toLowerCase()) {
      case "categories" -> {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
        c.name = request.name().trim();
        if (request.slug() != null && !request.slug().isBlank()) c.slug = EnumMapper.slugify(request.slug());
        if (request.description() != null) c.description = request.description();
        c = categoryRepository.save(c);
        yield new AdminDtos.TaxonomyEntryDto(c.id, c.name, c.slug, c.description, null, null);
      }
      case "departments" -> {
        Department d = departmentRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Department not found"));
        d.name = request.name().trim();
        if (request.code() != null) d.code = request.code();
        d = departmentRepository.save(d);
        yield new AdminDtos.TaxonomyEntryDto(d.id, d.name, null, null, d.code, d.school != null ? d.school.id : null);
      }
      case "schools" -> {
        School s = schoolRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "School not found"));
        s.name = request.name().trim();
        s = schoolRepository.save(s);
        yield new AdminDtos.TaxonomyEntryDto(s.id, s.name, null, null, null, null);
      }
      case "tags" -> {
        Tag t = tagRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tag not found"));
        t.name = request.name().trim();
        if (request.slug() != null && !request.slug().isBlank()) t.slug = EnumMapper.slugify(request.slug());
        t = tagRepository.save(t);
        yield new AdminDtos.TaxonomyEntryDto(t.id, t.name, t.slug, null, null, null);
      }
      case "content_types" -> {
        ContentTypeDef ct = contentTypeDefRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Content type not found"));
        ct.name = request.name().trim();
        if (request.slug() != null && !request.slug().isBlank())
          ct.slug = request.slug().trim().toLowerCase().replace('-', '_').replace(' ', '_');
        ct.description = request.description();
        ct = contentTypeDefRepository.save(ct);
        yield new AdminDtos.TaxonomyEntryDto(ct.id, ct.name, ct.slug, ct.description, null, null);
      }
      default -> throw new ResponseStatusException(BAD_REQUEST, "Unsupported taxonomy type");
    };
  }

  public void deleteTaxonomy(String type, UUID id) {
    authContextService.requireAdmin();
    AppUser user = authContextService.requireUser();
    switch (type.toLowerCase()) {
      case "categories" -> categoryRepository.deleteById(id);
      case "departments" -> departmentRepository.deleteById(id);
      case "schools" -> schoolRepository.deleteById(id);
      case "tags" -> tagRepository.deleteById(id);
      case "content_types" -> contentTypeDefRepository.deleteById(id);
      default -> throw new ResponseStatusException(BAD_REQUEST, "Unsupported taxonomy type");
    }
    auditService.log(user, "taxonomy." + type + ".delete", type, id.toString());
  }

  public List<AdminDtos.SystemSettingDto> settings() {
    authContextService.requireAdmin();
    return systemSettingRepository.findAll(Sort.by("settingKey")).stream()
      .map(setting -> new AdminDtos.SystemSettingDto(setting.id, setting.settingKey, setting.settingValue, setting.description))
      .toList();
  }

  public AdminDtos.SystemSettingDto updateSetting(String key, AdminDtos.UpdateSettingRequest request) {
    authContextService.requireAdmin();
    AppUser user = authContextService.requireUser();
    SystemSetting setting = systemSettingRepository.findBySettingKey(key).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Setting not found"));
    setting.settingValue = request.value();
    setting.updatedBy = user;
    setting = systemSettingRepository.save(setting);
    auditService.log(user, "settings.update", "system_settings", setting.id.toString());
    return new AdminDtos.SystemSettingDto(setting.id, setting.settingKey, setting.settingValue, setting.description);
  }

  public AdminDtos.ReportsDto reports() {
    authContextService.requireContentManager();
    List<ContentItem> items = contentItemRepository.findAll();
    List<ContentFile> files = contentFileRepository.findAll();
    Map<String, Long> byType = new LinkedHashMap<>();
    for (ContentItem item : items) {
      byType.merge(item.contentType, 1L, Long::sum);
    }
    long totalBytes = files.stream().mapToLong(file -> file.fileSize != null ? file.fileSize : 0L).sum();
    List<AdminDtos.RecentContentDto> recent = items.stream()
      .sorted(Comparator.comparing((ContentItem item) -> item.updatedAt).reversed())
      .limit(8)
      .map(this::recentContent)
      .toList();
    return new AdminDtos.ReportsDto(
      items.size(),
      items.stream().filter(item -> item.status == ContentStatus.PUBLISHED).count(),
      items.stream().filter(item -> item.status == ContentStatus.SUBMITTED).count(),
      items.stream().filter(item -> item.status == ContentStatus.DRAFT).count(),
      items.stream().filter(item -> item.status == ContentStatus.ARCHIVED).count(),
      eventRepository.count(),
      digitizationRecordRepository.count(),
      appUserRepository.count(),
      files.size(),
      formatBytes(totalBytes),
      viewRecordRepository.count(),
      downloadRecordRepository.count(),
      byType,
      recent
    );
  }

  public List<AdminDtos.DemonstrationSiteDto> adminSites() {
    authContextService.requireContentManager();
    return demonstrationSiteRepository.findAll(Sort.by("name")).stream()
      .map(this::adminSiteDto)
      .toList();
  }

  public AdminDtos.DemonstrationSiteDto saveSite(UUID id, AdminDtos.SaveDemonstrationSiteRequest request) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    DemonstrationSite site = id == null
      ? new DemonstrationSite()
      : demonstrationSiteRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Site not found"));

    site.name = request.name().trim();
    site.slug = uniqueSiteSlug(id, request.slug(), request.name());
    site.description = request.description();
    site.location = request.location();
    site.district = request.district();
    site.siteType = request.siteType();
    site.technology = request.technology();
    site.operationalStatus = request.operationalStatus() == null || request.operationalStatus().isBlank() ? "active" : request.operationalStatus();
    site.operationalSince = request.operationalSince();
    site.contactPerson = request.contactPerson();
    site.contactEmail = request.contactEmail();
    site.partnerInstitution = request.partnerInstitution();
    site.latitude = request.latitude();
    site.longitude = request.longitude();
    site.notes = request.notes();
    site = demonstrationSiteRepository.save(site);
    auditService.log(user, id == null ? "site.create" : "site.update", "demonstration_sites", site.id.toString());
    return adminSiteDto(site);
  }

  public void deleteSite(UUID id) {
    authContextService.requireAdmin();
    AppUser user = authContextService.requireUser();
    demonstrationSiteRepository.deleteById(id);
    auditService.log(user, "site.delete", "demonstration_sites", id.toString());
  }

  private String uniqueSiteSlug(UUID id, String requestedSlug, String name) {
    String base = requestedSlug != null && !requestedSlug.isBlank()
      ? EnumMapper.slugify(requestedSlug)
      : EnumMapper.slugify(name);
    String candidate = base;
    int i = 2;
    while (demonstrationSiteRepository.existsBySlug(candidate)
      && (id == null || !demonstrationSiteRepository.findBySlug(candidate).map(s -> s.id.equals(id)).orElse(false))) {
      candidate = base + "-" + i++;
    }
    return candidate;
  }

  private AdminDtos.DemonstrationSiteDto adminSiteDto(DemonstrationSite s) {
    return new AdminDtos.DemonstrationSiteDto(
      s.id, s.name, s.slug, s.description, s.location, s.district,
      s.siteType, s.technology, s.operationalStatus, s.operationalSince,
      s.contactPerson, s.contactEmail, s.partnerInstitution,
      s.latitude, s.longitude, s.notes, s.createdAt, s.updatedAt,
      s.imagePath != null ? "/api/files/site/" + s.id + "/image" : null
    );
  }

  public String uploadSiteImage(UUID id, MultipartFile file) {
    authContextService.requireContentManager();
    DemonstrationSite site = demonstrationSiteRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Site not found"));
    if (site.imagePath != null) {
      storageService.delete(site.imagePath);
    }
    site.imagePath = storageService.storeSiteImage(id, file);
    demonstrationSiteRepository.save(site);
    return "/api/files/site/" + id + "/image";
  }

  public List<AdminDtos.AuditLogDto> audit(String action, String entityType) {
    authContextService.requireAdmin();
    String actionFilter = (action != null && !action.isBlank()) ? action + "%" : null;
    String typeFilter = (entityType != null && !entityType.isBlank()) ? entityType : null;
    return auditLogRepository.findFiltered(actionFilter, typeFilter).stream()
      .map(log -> new AdminDtos.AuditLogDto(
        log.id,
        log.user != null ? log.user.id.toString() : null,
        log.user != null ? log.user.email : null,
        log.action,
        log.entityType,
        log.entityId,
        log.oldValue,
        log.newValue,
        log.ipAddress,
        log.createdAt
      ))
      .toList();
  }

  private AdminDtos.RecentContentDto recentContent(ContentItem item) {
    return new AdminDtos.RecentContentDto(item.id, item.title, item.status.apiValue(), item.updatedAt);
  }

  private AdminDtos.ContentEditorDto contentEditorDto(ContentItem item) {
    return new AdminDtos.ContentEditorDto(
      item.id,
      item.title,
      item.slug,
      item.summary,
      item.body,
      item.contentType,
      item.status.apiValue(),
      item.accessLevel.apiValue(),
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
      item.category != null ? item.category.id.toString() : null,
      item.department != null ? item.department.id.toString() : null,
      item.author != null ? item.author.id.toString() : null,
      item.publishedAt,
      item.thumbnailPath != null ? "/api/files/thumbnail/" + item.id : null
    );
  }

  public String uploadThumbnail(UUID id, MultipartFile file) {
    authContextService.requireContentManager();
    ContentItem item = contentItemRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Content not found"));
    if (item.thumbnailPath != null) {
      storageService.delete(item.thumbnailPath);
    }
    item.thumbnailPath = storageService.storeThumbnail(id, file);
    contentItemRepository.save(item);
    return "/api/files/thumbnail/" + id;
  }

  public void updateUserStatus(UUID userId, String status) {
    authContextService.requireAdmin();
    AppUser user = appUserRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    if (!List.of("active", "suspended", "pending").contains(status)) {
      throw new ResponseStatusException(BAD_REQUEST, "Invalid status");
    }
    user.status = status;
    appUserRepository.save(user);
  }

  public void deleteUser(UUID userId) {
    authContextService.requireAdmin();
    AppUser actor = authContextService.requireUser();

    if (actor.id.equals(userId)) {
      throw new ResponseStatusException(BAD_REQUEST, "You cannot delete your own account");
    }

    AppUser target = appUserRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

    boolean targetIsSuperAdmin = target.roles.stream()
      .anyMatch(r -> r.role == AppRole.SUPER_ADMIN);
    if (targetIsSuperAdmin && userRoleRepository.countByRole(AppRole.SUPER_ADMIN) <= 1) {
      throw new ResponseStatusException(BAD_REQUEST, "Cannot delete the last system administrator");
    }

    // Nullify nullable user references in analytics/audit tables
    viewRecordRepository.nullifyUser(userId);
    downloadRecordRepository.nullifyUser(userId);
    auditLogRepository.nullifyUser(userId);
    contentItemRepository.nullifyAuthor(userId);

    // Delete owned records
    trainingRegistrationRepository.deleteAll(trainingRegistrationRepository.findByUserId(userId));
    notificationRepository.deleteByUser_Id(userId);
    bookmarkRepository.deleteAll(bookmarkRepository.findByUser_IdOrderByCreatedAtDesc(userId));

    // Reading lists: items first, then lists (user_id NOT NULL)
    readingListRepository.findByUser_IdOrderByUpdatedAtDesc(userId).forEach(rl ->
      readingListItemRepository.deleteByReadingList_Id(rl.id)
    );
    readingListRepository.deleteAll(readingListRepository.findByUser_IdOrderByUpdatedAtDesc(userId));

    // Role assignments, sessions, and the user itself
    userSessionRepository.deleteByUser_Id(userId);
    userRoleRepository.deleteAll(userRoleRepository.findByUser_Id(userId));
    appUserRepository.delete(target);

    auditService.log(actor, "user.delete", "users", userId.toString());
  }

  public List<AdminDtos.SessionDto> sessions() {
    authContextService.requireAdmin();
    return userSessionRepository.findRecentWithUser().stream()
      .map(s -> {
        boolean active = s.revokedAt == null
          && s.expiresAt.isAfter(Instant.now())
          && s.sessionId.equals(s.user.currentSessionId);
        return new AdminDtos.SessionDto(
          s.id,
          s.user.id,
          s.user.fullName,
          s.user.email,
          s.ipAddress,
          s.userAgent,
          s.createdAt,
          s.expiresAt,
          active,
          s.revokedAt
        );
      })
      .toList();
  }

  public void revokeUserSession(UUID userId) {
    authContextService.requireAdmin();
    AppUser user = appUserRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    // Invalidate JWT-level session
    user.currentSessionId = null;
    appUserRepository.save(user);
    // Mark session records as revoked
    userSessionRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
      .filter(s -> s.revokedAt == null)
      .forEach(s -> { s.revokedAt = Instant.now(); userSessionRepository.save(s); });
    auditService.log(authContextService.requireUser(), "session.revoke", "users", userId.toString());
  }

  public void adminResetPassword(UUID userId, String newPassword) {
    authContextService.requireAdmin();
    AppUser actor = authContextService.requireUser();
    AppUser user = appUserRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    if (user.authType != AuthType.LOCAL) {
      throw new ResponseStatusException(BAD_REQUEST,
        "Password can only be reset for local accounts. UNZA students and staff authenticate through their own systems.");
    }
    user.passwordHash = passwordEncoder.encode(newPassword);
    // Invalidate current session — user must re-login with the new password
    user.currentSessionId = null;
    appUserRepository.save(user);
    // Revoke session records
    userSessionRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
      .filter(s -> s.revokedAt == null)
      .forEach(s -> { s.revokedAt = Instant.now(); userSessionRepository.save(s); });
    auditService.log(actor, "user.password.reset", "users", userId.toString());
  }

  private AdminDtos.EventAdminDto eventAdminDto(EventEntity event) {
    return new AdminDtos.EventAdminDto(
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

  public AdminDtos.DigitizationDto advanceDigitizationStatus(UUID id, String newStatus) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    DigitizationRecord record = digitizationRecordRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Digitization record not found"));
    DigitizationStatus status = EnumMapper.digitizationStatus(newStatus);
    record.digitizationStatus = status;
    // Track who reviewed when moving into review / approval / rejection stages
    if (status == DigitizationStatus.UNDER_REVIEW || status == DigitizationStatus.APPROVED
        || status == DigitizationStatus.REJECTED) {
      record.reviewedBy = user;
    }
    DigitizationRecord saved = digitizationRecordRepository.save(record);
    auditService.log(user, "digitization.status." + newStatus, "digitization_records", saved.id.toString());
    return digitizationDto(saved);
  }

  @org.springframework.transaction.annotation.Transactional
  public AdminDtos.PromoteResult promoteToLibrary(UUID digitizationId) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    DigitizationRecord rec = digitizationRecordRepository.findById(digitizationId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Digitization record not found"));

    if (rec.digitizationStatus != DigitizationStatus.APPROVED) {
      throw new ResponseStatusException(BAD_REQUEST, "Only approved records can be promoted. Current status: " + rec.digitizationStatus.apiValue());
    }
    if (rec.contentItem != null) {
      throw new ResponseStatusException(BAD_REQUEST, "This record has already been promoted to the library (content item " + rec.contentItem.id + ").");
    }

    // ── Create the ContentItem ──────────────────────────────────────
    ContentItem item = new ContentItem();
    item.title = rec.title;
    item.summary = rec.description;
    item.contentType = "digitized";
    item.status = ContentStatus.DRAFT;
    item.accessLevel = copyrightToAccessLevel(rec.copyrightStatus);
    item.downloadable = true;
    item.copyrightStatus = rec.copyrightStatus;
    item.slug = uniqueSlug(null, null, rec.title, false);
    item = contentItemRepository.save(item);

    // ── Attach the scanned file as a ContentFile ────────────────────
    if (rec.scannedFilePath != null) {
      ContentFile cf = new ContentFile();
      cf.contentItem = item;
      cf.filePath = rec.scannedFilePath;
      cf.storedFileName = java.nio.file.Paths.get(rec.scannedFilePath).getFileName().toString();
      cf.originalFileName = cf.storedFileName;
      cf.fileExtension = cf.storedFileName.contains(".")
        ? cf.storedFileName.substring(cf.storedFileName.lastIndexOf('.') + 1).toLowerCase()
        : "";
      cf.mimeType = storageService.mediaType(cf.originalFileName, null).toString();
      cf.fileType = cf.mimeType.contains("/") ? cf.mimeType.split("/")[0] : null;
      cf.accessLevel = item.accessLevel;
      cf.uploadedBy = user;
      try {
        cf.fileSize = java.nio.file.Files.size(storageService.load(rec.scannedFilePath).getFile().toPath());
      } catch (Exception ignored) {}
      contentFileRepository.save(cf);

      // Auto-generate thumbnail (e.g. PDF first page)
      try {
        final ContentItem savedItem = item;
        thumbnailService.generate(
          storageService.load(rec.scannedFilePath).getFile().toPath(),
          cf.mimeType, savedItem.id
        ).ifPresent(thumbPath -> {
          savedItem.thumbnailPath = thumbPath;
          contentItemRepository.save(savedItem);
        });
      } catch (Exception ignored) {}
    }

    // ── Link the digitization record and mark published ─────────────
    rec.contentItem = item;
    rec.digitizationStatus = DigitizationStatus.PUBLISHED;
    DigitizationRecord saved = digitizationRecordRepository.save(rec);

    auditService.log(user, "digitization.promote", "digitization_records", saved.id.toString());
    auditService.log(user, "content.create", "content_items", item.id.toString());

    return new AdminDtos.PromoteResult(item.id, digitizationDto(saved));
  }

  private AccessLevel copyrightToAccessLevel(String copyrightStatus) {
    return AccessLevel.PUBLIC;
  }

  public String getScanFilePath(UUID recordId) {
    return digitizationRecordRepository.findById(recordId)
      .map(r -> r.scannedFilePath)
      .orElse(null);
  }

  public org.springframework.core.io.Resource loadScanFile(String relativePath) {
    return storageService.load(relativePath);
  }

  private AdminDtos.DigitizationDto digitizationDto(DigitizationRecord record) {
    return new AdminDtos.DigitizationDto(
      record.id,
      record.title,
      record.description,
      record.sourceOwner,
      record.physicalLocation,
      record.numberOfPages,
      record.digitizationStatus.apiValue(),
      record.scannedFilePath,
      record.scannedFilePath != null ? "/api/admin/digitization/" + record.id + "/scan-file" : null,
      record.ocrText,
      record.qualityStatus,
      record.copyrightStatus,
      record.contentItem != null ? record.contentItem.id : null,
      record.scannedBy != null ? record.scannedBy.fullName : null,
      record.reviewedBy != null ? record.reviewedBy.fullName : null,
      record.createdAt,
      record.updatedAt
    );
  }

  private Category nullableCategory(String categoryId) {
    if (categoryId == null || categoryId.isBlank()) {
      return null;
    }
    return categoryRepository.findById(UUID.fromString(categoryId)).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
  }

  private Department nullableDepartment(String departmentId) {
    if (departmentId == null || departmentId.isBlank()) {
      return null;
    }
    return departmentRepository.findById(UUID.fromString(departmentId)).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Department not found"));
  }

  private String uniqueSlug(UUID id, String slug, String fallbackTitle, boolean event) {
    String base = (slug == null || slug.isBlank()) ? EnumMapper.slugify(fallbackTitle) : EnumMapper.slugify(slug);
    String candidate = base.isBlank() ? "item" : base;
    int counter = 2;
    while (slugExists(candidate, id, event)) {
      candidate = base + "-" + counter++;
    }
    return candidate;
  }

  private boolean slugExists(String candidate, UUID id, boolean event) {
    if (event) {
      return eventRepository.findBySlug(candidate).filter(existing -> id == null || !existing.id.equals(id)).isPresent();
    }
    return contentItemRepository.findBySlug(candidate).filter(existing -> id == null || !existing.id.equals(id)).isPresent();
  }

  private String formatBytes(long bytes) {
    if (bytes <= 0) {
      return "0 B";
    }
    String[] units = {"B", "KB", "MB", "GB", "TB"};
    double value = bytes;
    int index = 0;
    while (value >= 1024 && index < units.length - 1) {
      value /= 1024;
      index++;
    }
    return (value >= 10 ? Math.round(value) : Math.round(value * 10.0) / 10.0) + " " + units[index];
  }

  public List<AdminDtos.TrainingSessionDto> adminTrainingSessions() {
    authContextService.requireContentManager();
    return trainingSessionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
      .map(s -> trainingSessionDto(s, trainingRegistrationRepository.countActiveBySessionId(s.id)))
      .toList();
  }

  public AdminDtos.TrainingSessionDto saveTrainingSession(UUID id, AdminDtos.SaveTrainingSessionRequest request) {
    authContextService.requireContentManager();
    AppUser user = authContextService.requireUser();
    TrainingSession session = id == null
      ? new TrainingSession()
      : trainingSessionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Training session not found"));

    session.title = request.title().trim();
    session.slug = uniqueTrainingSlug(id, request.slug(), request.title());
    session.description = request.description();
    session.mode = request.mode();
    session.venue = request.venue();
    session.instructor = request.instructor();
    session.startDate = request.startDate();
    session.endDate = request.endDate();
    session.startTime = request.startTime();
    session.endTime = request.endTime();
    session.capacity = request.capacity();
    session.requirements = request.requirements();
    session.contactEmail = request.contactEmail();
    if (request.status() != null && !request.status().isBlank()) {
      session.status = EnumMapper.status(request.status());
    }
    if (id == null) {
      session.createdBy = user;
    }
    if (session.status == ContentStatus.PUBLISHED && session.publishedAt == null) {
      session.publishedAt = java.time.Instant.now();
    }
    session = trainingSessionRepository.save(session);
    auditService.log(user, id == null ? "training.create" : "training.update", "training_sessions", session.id.toString());
    return trainingSessionDto(session, trainingRegistrationRepository.countActiveBySessionId(session.id));
  }

  public void publishTrainingSession(UUID id) {
    authContextService.requireAdmin();
    AppUser user = authContextService.requireUser();
    TrainingSession session = trainingSessionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Training session not found"));
    session.status = ContentStatus.PUBLISHED;
    session.publishedAt = java.time.Instant.now();
    trainingSessionRepository.save(session);
    auditService.log(user, "training.publish", "training_sessions", id.toString());
  }

  public void deleteTrainingSession(UUID id) {
    authContextService.requireAdmin();
    AppUser user = authContextService.requireUser();
    trainingRegistrationRepository.deleteAll(trainingRegistrationRepository.findBySessionId(id));
    trainingSessionRepository.deleteById(id);
    auditService.log(user, "training.delete", "training_sessions", id.toString());
  }

  public List<AdminDtos.TrainingRegistrationAdminDto> trainingRegistrations(UUID sessionId) {
    authContextService.requireContentManager();
    return trainingRegistrationRepository.findBySessionId(sessionId).stream()
      .map(r -> new AdminDtos.TrainingRegistrationAdminDto(
        r.id,
        r.user.id,
        r.user.fullName,
        r.user.email,
        r.status,
        r.registeredAt
      ))
      .toList();
  }

  private String uniqueTrainingSlug(UUID id, String requestedSlug, String name) {
    String base = requestedSlug != null && !requestedSlug.isBlank()
      ? EnumMapper.slugify(requestedSlug)
      : EnumMapper.slugify(name);
    String candidate = base.isBlank() ? "training" : base;
    int i = 2;
    while (trainingSessionRepository.existsBySlug(candidate)
      && (id == null || !trainingSessionRepository.findBySlug(candidate).map(s -> s.id.equals(id)).orElse(false))) {
      candidate = base + "-" + i++;
    }
    return candidate;
  }

  private AdminDtos.TrainingSessionDto trainingSessionDto(TrainingSession s, long regCount) {
    return new AdminDtos.TrainingSessionDto(
      s.id, s.title, s.slug, s.description, s.mode, s.venue, s.instructor,
      s.startDate, s.endDate, s.startTime, s.endTime, s.capacity,
      s.requirements, s.contactEmail, s.status.apiValue(), s.publishedAt, s.createdAt, regCount
    );
  }

  public String generateReportCsv() {
    authContextService.requireAdmin();
    var sb = new StringBuilder();
    var now = java.time.Instant.now().toString();

    // ── Summary ─────────────────────────────────────────────────────────────
    sb.append("UNZA SANITATION HUB — SYSTEM REPORT\n");
    sb.append("Generated,").append(now).append("\n\n");

    List<ContentItem> items = contentItemRepository.findAll();
    List<ContentFile> files = contentFileRepository.findAll();
    long totalBytes = files.stream().mapToLong(f -> f.fileSize != null ? f.fileSize : 0L).sum();

    sb.append("SUMMARY\n");
    appendCsvRow(sb, "Metric", "Value");
    appendCsvRow(sb, "Total resources", String.valueOf(items.size()));
    appendCsvRow(sb, "Published resources", String.valueOf(items.stream().filter(i -> i.status == ContentStatus.PUBLISHED).count()));
    appendCsvRow(sb, "Pending approval", String.valueOf(items.stream().filter(i -> i.status == ContentStatus.SUBMITTED).count()));
    appendCsvRow(sb, "Draft resources", String.valueOf(items.stream().filter(i -> i.status == ContentStatus.DRAFT).count()));
    appendCsvRow(sb, "Archived resources", String.valueOf(items.stream().filter(i -> i.status == ContentStatus.ARCHIVED).count()));
    appendCsvRow(sb, "Events", String.valueOf(eventRepository.count()));
    appendCsvRow(sb, "Training sessions", String.valueOf(trainingSessionRepository.count()));
    appendCsvRow(sb, "Digitization records", String.valueOf(digitizationRecordRepository.count()));
    appendCsvRow(sb, "Registered users", String.valueOf(appUserRepository.count()));
    appendCsvRow(sb, "Files stored", String.valueOf(files.size()));
    appendCsvRow(sb, "Total storage", formatBytes(totalBytes));
    appendCsvRow(sb, "Total views", String.valueOf(viewRecordRepository.count()));
    appendCsvRow(sb, "Total downloads", String.valueOf(downloadRecordRepository.count()));
    sb.append("\n");

    // ── Resources ────────────────────────────────────────────────────────────
    Map<UUID, Long> viewsByItem = new java.util.HashMap<>();
    for (Object[] row : viewRecordRepository.countGroupedByContentItem()) {
      viewsByItem.put((UUID) row[0], (Long) row[1]);
    }
    Map<UUID, Long> downloadsByItem = new java.util.HashMap<>();
    for (Object[] row : downloadRecordRepository.countGroupedByContentItem()) {
      downloadsByItem.put((UUID) row[0], (Long) row[1]);
    }

    sb.append("RESOURCES\n");
    appendCsvRow(sb, "Title", "Content Type", "Status", "Access", "Author", "Views", "Downloads", "Created", "Updated");
    for (ContentItem item : items.stream().sorted(Comparator.comparing((ContentItem i) -> i.createdAt).reversed()).toList()) {
      long views = viewsByItem.getOrDefault(item.id, 0L);
      long downloads = downloadsByItem.getOrDefault(item.id, 0L);
      appendCsvRow(sb,
        item.title,
        item.contentType,
        item.status.apiValue(),
        item.accessLevel.apiValue(),
        item.author != null ? item.author.fullName : "",
        String.valueOf(views),
        String.valueOf(downloads),
        item.createdAt != null ? item.createdAt.toString() : "",
        item.updatedAt != null ? item.updatedAt.toString() : ""
      );
    }
    sb.append("\n");

    // ── Users ────────────────────────────────────────────────────────────────
    sb.append("USERS\n");
    appendCsvRow(sb, "Full Name", "Email", "Roles", "Status", "Auth Type", "Registered", "Last Login");
    for (AppUser user : appUserRepository.findAll(Sort.by("createdAt"))) {
      String roles = user.roles.stream().map(r -> r.role.name()).collect(java.util.stream.Collectors.joining("|"));
      appendCsvRow(sb,
        user.fullName != null ? user.fullName : "",
        user.email,
        roles,
        user.status,
        user.authType != null ? user.authType.name() : "",
        user.createdAt != null ? user.createdAt.toString() : "",
        user.lastLoginAt != null ? user.lastLoginAt.toString() : ""
      );
    }
    sb.append("\n");

    // ── Events ───────────────────────────────────────────────────────────────
    sb.append("EVENTS\n");
    appendCsvRow(sb, "Title", "Status", "Start Date", "End Date", "Venue", "Created");
    for (EventEntity ev : eventRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate"))) {
      appendCsvRow(sb,
        ev.title,
        ev.status.apiValue(),
        ev.startDate != null ? ev.startDate.toString() : "",
        ev.endDate != null ? ev.endDate.toString() : "",
        ev.venue != null ? ev.venue : "",
        ev.createdAt != null ? ev.createdAt.toString() : ""
      );
    }
    sb.append("\n");

    // ── Training sessions ────────────────────────────────────────────────────
    sb.append("TRAINING SESSIONS\n");
    appendCsvRow(sb, "Title", "Mode", "Status", "Start Date", "End Date", "Venue", "Capacity", "Registrations", "Created");
    for (TrainingSession s : trainingSessionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))) {
      long regCount = trainingRegistrationRepository.countActiveBySessionId(s.id);
      appendCsvRow(sb,
        s.title,
        s.mode != null ? s.mode : "",
        s.status.apiValue(),
        s.startDate != null ? s.startDate.toString() : "",
        s.endDate != null ? s.endDate.toString() : "",
        s.venue != null ? s.venue : "",
        s.capacity != null ? String.valueOf(s.capacity) : "Unlimited",
        String.valueOf(regCount),
        s.createdAt != null ? s.createdAt.toString() : ""
      );
    }
    sb.append("\n");

    // ── Demonstration sites ──────────────────────────────────────────────────
    sb.append("DEMONSTRATION SITES\n");
    appendCsvRow(sb, "Name", "Type", "Operational Status", "District", "Location", "Since");
    for (DemonstrationSite site : demonstrationSiteRepository.findAll(Sort.by("name"))) {
      appendCsvRow(sb,
        site.name,
        site.siteType != null ? site.siteType : "",
        site.operationalStatus != null ? site.operationalStatus : "",
        site.district != null ? site.district : "",
        site.location != null ? site.location : "",
        site.operationalSince != null ? site.operationalSince.toString() : ""
      );
    }

    return sb.toString();
  }

  private void appendCsvRow(StringBuilder sb, String... fields) {
    for (int i = 0; i < fields.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(escapeCsv(fields[i]));
    }
    sb.append('\n');
  }

  private String escapeCsv(String value) {
    if (value == null) return "";
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}
