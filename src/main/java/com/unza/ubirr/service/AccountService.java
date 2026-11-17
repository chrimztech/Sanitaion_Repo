package com.unza.ubirr.service;

import com.unza.ubirr.dto.AccountDtos;
import com.unza.ubirr.dto.PublicDtos;
import com.unza.ubirr.model.AppUser;
import com.unza.ubirr.model.Bookmark;
import com.unza.ubirr.model.ContentItem;
import com.unza.ubirr.model.ContentStatus;
import com.unza.ubirr.model.EventEntity;
import com.unza.ubirr.model.EventRegistration;
import com.unza.ubirr.model.Notification;
import com.unza.ubirr.model.ReadingList;
import com.unza.ubirr.model.ReadingListItem;
import com.unza.ubirr.model.Review;
import com.unza.ubirr.model.TrainingRegistration;
import com.unza.ubirr.model.TrainingSession;
import com.unza.ubirr.model.ViewRecord;
import com.unza.ubirr.repository.AppUserRepository;
import com.unza.ubirr.repository.BookmarkRepository;
import com.unza.ubirr.repository.ContentItemRepository;
import com.unza.ubirr.repository.EventRegistrationRepository;
import com.unza.ubirr.repository.EventRepository;
import com.unza.ubirr.repository.NotificationRepository;
import com.unza.ubirr.repository.ReadingListItemRepository;
import com.unza.ubirr.repository.ReadingListRepository;
import com.unza.ubirr.repository.ReviewRepository;
import com.unza.ubirr.repository.TrainingRegistrationRepository;
import com.unza.ubirr.repository.TrainingSessionRepository;
import com.unza.ubirr.repository.ViewRecordRepository;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class AccountService {
  private final AuthContextService authContextService;
  private final AppUserRepository appUserRepository;
  private final BookmarkRepository bookmarkRepository;
  private final ContentItemRepository contentItemRepository;
  private final ViewRecordRepository viewRecordRepository;
  private final NotificationRepository notificationRepository;
  private final ReadingListRepository readingListRepository;
  private final ReadingListItemRepository readingListItemRepository;
  private final TrainingSessionRepository trainingSessionRepository;
  private final TrainingRegistrationRepository trainingRegistrationRepository;
  private final EventRepository eventRepository;
  private final EventRegistrationRepository eventRegistrationRepository;
  private final PublicService publicService;
  private final ReviewRepository reviewRepository;

  public AccountService(
    AuthContextService authContextService,
    AppUserRepository appUserRepository,
    BookmarkRepository bookmarkRepository,
    ContentItemRepository contentItemRepository,
    ViewRecordRepository viewRecordRepository,
    NotificationRepository notificationRepository,
    ReadingListRepository readingListRepository,
    ReadingListItemRepository readingListItemRepository,
    TrainingSessionRepository trainingSessionRepository,
    TrainingRegistrationRepository trainingRegistrationRepository,
    EventRepository eventRepository,
    EventRegistrationRepository eventRegistrationRepository,
    PublicService publicService,
    ReviewRepository reviewRepository
  ) {
    this.authContextService = authContextService;
    this.appUserRepository = appUserRepository;
    this.bookmarkRepository = bookmarkRepository;
    this.contentItemRepository = contentItemRepository;
    this.viewRecordRepository = viewRecordRepository;
    this.notificationRepository = notificationRepository;
    this.readingListRepository = readingListRepository;
    this.readingListItemRepository = readingListItemRepository;
    this.trainingSessionRepository = trainingSessionRepository;
    this.trainingRegistrationRepository = trainingRegistrationRepository;
    this.eventRepository = eventRepository;
    this.eventRegistrationRepository = eventRegistrationRepository;
    this.publicService = publicService;
    this.reviewRepository = reviewRepository;
  }

  public AccountDtos.ProfileDto profile() {
    AppUser user = authContextService.requireUser();
    return new AccountDtos.ProfileDto(
      user.id,
      user.email,
      user.fullName,
      user.phone,
      user.affiliation,
      user.institution,
      user.jobTitle,
      user.status,
      user.createdAt,
      user.lastLoginAt,
      user.roles.stream().map(role -> role.role.apiValue()).toList()
    );
  }

  public AccountDtos.ProfileDto updateProfile(AccountDtos.UpdateProfileRequest request) {
    AppUser user = authContextService.requireUser();
    user.fullName = request.fullName().trim();
    if (request.phone() != null) user.phone = request.phone().isBlank() ? null : request.phone().trim();
    if (request.institution() != null) user.institution = request.institution().isBlank() ? null : request.institution().trim();
    if (request.jobTitle() != null) user.jobTitle = request.jobTitle().isBlank() ? null : request.jobTitle().trim();
    appUserRepository.save(user);
    return profile();
  }

  public List<AccountDtos.BookmarkDto> bookmarks() {
    AppUser user = authContextService.requireUser();
    return bookmarkRepository.findByUser_IdOrderByCreatedAtDesc(user.id).stream()
      .filter(bookmark -> bookmark.contentItem.deletedAt == null && bookmark.contentItem.title != null && bookmark.contentItem.slug != null)
      .map(bookmark -> new AccountDtos.BookmarkDto(bookmark.id, bookmark.createdAt, publicService.summary(bookmark.contentItem)))
      .toList();
  }

  @CacheEvict(value = {"public-resources", "public-resource", "public-home", "public-gallery"}, allEntries = true)
  public PublicDtos.ReviewDto upsertReview(UUID contentId, AccountDtos.CreateReviewRequest request) {
    AppUser user = authContextService.requireUser();
    ContentItem item = contentItemRepository.findById(contentId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resource not found"));
    Review review = reviewRepository.findByContentItem_IdAndUser_Id(contentId, user.id).orElseGet(Review::new);
    review.contentItem = item;
    review.user = user;
    review.rating = request.rating();
    review.body = request.body() == null || request.body().isBlank() ? null : request.body().trim();
    Review saved = reviewRepository.save(review);
    return new PublicDtos.ReviewDto(saved.id, user.fullName, true, saved.rating, saved.body, saved.createdAt, saved.updatedAt);
  }

  @CacheEvict(value = {"public-resources", "public-resource", "public-home", "public-gallery"}, allEntries = true)
  public void deleteOwnReview(UUID contentId) {
    AppUser user = authContextService.requireUser();
    reviewRepository.findByContentItem_IdAndUser_Id(contentId, user.id)
      .ifPresent(reviewRepository::delete);
  }

  public AccountDtos.BookmarkStatusDto bookmarkStatus(UUID contentId) {
    AppUser user = authContextService.requireUser();
    return new AccountDtos.BookmarkStatusDto(bookmarkRepository.existsByUser_IdAndContentItem_Id(user.id, contentId));
  }

  public void addBookmark(UUID contentId) {
    AppUser user = authContextService.requireUser();
    if (bookmarkRepository.existsByUser_IdAndContentItem_Id(user.id, contentId)) {
      return;
    }
    ContentItem item = contentItemRepository.findById(contentId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resource not found"));
    Bookmark bookmark = new Bookmark();
    bookmark.user = user;
    bookmark.contentItem = item;
    bookmarkRepository.save(bookmark);
  }

  public void removeBookmark(UUID contentId) {
    AppUser user = authContextService.requireUser();
    bookmarkRepository.findByUser_IdAndContentItem_Id(user.id, contentId).ifPresent(bookmarkRepository::delete);
  }

  public List<AccountDtos.RecentViewDto> recentViews() {
    AppUser user = authContextService.requireUser();
    List<ViewRecord> records = viewRecordRepository.findTop50ByUser_IdOrderByViewedAtDesc(user.id);
    Set<UUID> seen = new LinkedHashSet<>();
    return records.stream()
      .filter(record -> record.contentItem != null && record.contentItem.deletedAt == null && record.contentItem.title != null && record.contentItem.slug != null && seen.add(record.contentItem.id))
      .limit(10)
      .map(record -> new AccountDtos.RecentViewDto(record.viewedAt, publicService.summary(record.contentItem)))
      .toList();
  }

  public List<AccountDtos.NotificationDto> notifications() {
    AppUser user = authContextService.requireUser();
    return notificationRepository.findTop100ByUser_IdOrderByCreatedAtDesc(user.id).stream()
      .map(this::notificationDto)
      .toList();
  }

  public void markNotificationsRead(AccountDtos.MarkNotificationsReadRequest request) {
    AppUser user = authContextService.requireUser();
    List<Notification> notifications = notificationRepository.findTop100ByUser_IdOrderByCreatedAtDesc(user.id);
    for (Notification notification : notifications) {
      if (request.all() || (request.ids() != null && request.ids().contains(notification.id))) {
        notification.read = true;
      }
    }
    notificationRepository.saveAll(notifications);
  }

  public List<AccountDtos.ReadingListDto> readingLists() {
    AppUser user = authContextService.requireUser();
    return readingListRepository.findByUser_IdOrderByUpdatedAtDesc(user.id).stream()
      .map(this::readingListDto)
      .toList();
  }

  public AccountDtos.ReadingListDto createReadingList(AccountDtos.SaveReadingListRequest request) {
    AppUser user = authContextService.requireUser();
    ReadingList list = new ReadingList();
    list.user = user;
    list.name = request.name().trim();
    list.description = request.description();
    list.publicList = request.isPublic();
    return readingListDto(readingListRepository.save(list));
  }

  public AccountDtos.ReadingListDto updateReadingList(UUID id, AccountDtos.SaveReadingListRequest request) {
    AppUser user = authContextService.requireUser();
    ReadingList list = ownedList(id, user.id);
    list.name = request.name().trim();
    list.description = request.description();
    list.publicList = request.isPublic();
    return readingListDto(readingListRepository.save(list));
  }

  public void deleteReadingList(UUID id) {
    AppUser user = authContextService.requireUser();
    ReadingList list = ownedList(id, user.id);
    // Children must go first — reading_list_items has a not-null FK to reading_lists,
    // so deleting the parent while items still reference it violates that constraint.
    readingListItemRepository.deleteByReadingList_Id(list.id);
    readingListRepository.delete(list);
  }

  public AccountDtos.ReadingListDto addReadingListItem(UUID listId, AccountDtos.SaveReadingListItemRequest request) {
    AppUser user = authContextService.requireUser();
    ReadingList list = ownedList(listId, user.id);
    ContentItem content = contentItemRepository.findById(request.contentId()).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resource not found"));
    ReadingListItem item = new ReadingListItem();
    item.readingList = list;
    item.contentItem = content;
    item.note = request.note();
    item.position = request.position() != null ? request.position() : readingListItemRepository.findByReadingList_IdOrderByPositionAsc(listId).size();
    readingListItemRepository.save(item);
    return readingListDto(list);
  }

  public void removeReadingListItem(UUID itemId) {
    AppUser user = authContextService.requireUser();
    // findByIdWithOwner fetch-joins readingList + its user — item.readingList.user.id would
    // otherwise walk two cold lazy associations at once and NPE (see the repository for why).
    ReadingListItem item = readingListItemRepository.findByIdWithOwner(itemId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reading list item not found"));
    if (!item.readingList.user.id.equals(user.id)) {
      throw new ResponseStatusException(NOT_FOUND, "Reading list item not found");
    }
    readingListItemRepository.delete(item);
  }

  private AccountDtos.NotificationDto notificationDto(Notification notification) {
    return new AccountDtos.NotificationDto(
      notification.id,
      notification.title,
      notification.message,
      notification.link,
      notification.read,
      notification.createdAt
    );
  }

  private ReadingList ownedList(UUID listId, UUID userId) {
    return readingListRepository.findById(listId)
      .filter(list -> list.user.id.equals(userId))
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reading list not found"));
  }

  private AccountDtos.ReadingListDto readingListDto(ReadingList list) {
    List<AccountDtos.ReadingListItemDto> items = readingListItemRepository.findByReadingList_IdOrderByPositionAsc(list.id).stream()
      .map(item -> new AccountDtos.ReadingListItemDto(
        item.id,
        item.contentItem.id,
        item.contentItem.title,
        item.contentItem.slug,
        item.note,
        item.position,
        item.addedAt
      ))
      .toList();
    return new AccountDtos.ReadingListDto(list.id, list.name, list.description, list.publicList, list.createdAt, list.updatedAt, items);
  }

  public List<AccountDtos.TrainingSessionPublicDto> trainingSessionsForUser() {
    AppUser user = authContextService.requireUser();
    List<TrainingSession> sessions = trainingSessionRepository.findByStatusIn(
      List.of(ContentStatus.PUBLISHED, ContentStatus.COMPLETED));
    return sessions.stream()
      .map(s -> {
        long count = trainingRegistrationRepository.countActiveBySessionId(s.id);
        boolean registered = trainingRegistrationRepository
          .findBySessionIdAndUserId(s.id, user.id)
          .map(r -> "registered".equals(r.status))
          .orElse(false);
        return new AccountDtos.TrainingSessionPublicDto(
          s.id, s.title, s.slug, s.description, s.mode, s.venue, s.instructor,
          s.startDate, s.endDate, s.startTime, s.endTime, s.capacity,
          s.requirements, s.contactEmail, s.status.apiValue(), s.publishedAt,
          count, registered,
          s.imagePath != null ? "/api/files/training/" + s.id + "/image" : null
        );
      })
      .toList();
  }

  public void registerForTraining(UUID sessionId) {
    AppUser user = authContextService.requireUser();
    TrainingSession session = trainingSessionRepository.findById(sessionId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Training session not found"));
    if (session.status != ContentStatus.PUBLISHED) {
      throw new ResponseStatusException(BAD_REQUEST, "Registration is not open for this training session");
    }
    var existing = trainingRegistrationRepository.findBySessionIdAndUserId(sessionId, user.id);
    if (existing.isPresent()) {
      if ("registered".equals(existing.get().status)) {
        throw new ResponseStatusException(CONFLICT, "Already registered for this session");
      }
      existing.get().status = "registered";
      trainingRegistrationRepository.save(existing.get());
      return;
    }
    if (session.capacity != null) {
      long count = trainingRegistrationRepository.countActiveBySessionId(sessionId);
      if (count >= session.capacity) {
        throw new ResponseStatusException(BAD_REQUEST, "This training session is fully booked");
      }
    }
    TrainingRegistration reg = new TrainingRegistration();
    reg.session = session;
    reg.user = user;
    reg.status = "registered";
    trainingRegistrationRepository.save(reg);
    notifyUser(user, "Registration confirmed",
      "You're registered for \"" + session.title + "\" on " + session.startDate + ".", "/training");
  }

  public void cancelTrainingRegistration(UUID sessionId) {
    AppUser user = authContextService.requireUser();
    TrainingRegistration reg = trainingRegistrationRepository.findBySessionIdAndUserId(sessionId, user.id)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Registration not found"));
    reg.status = "cancelled";
    trainingRegistrationRepository.save(reg);
    // reg.session is a lazy proxy here (findBySessionIdAndUserId doesn't fetch it) — load
    // the title explicitly rather than risk the blank-proxy-field pitfall described above.
    String title = trainingSessionRepository.findById(sessionId).map(s -> s.title).orElse("the session");
    notifyUser(user, "Registration cancelled", "Your registration for \"" + title + "\" was cancelled.", "/training");
  }

  public List<AccountDtos.TrainingRegistrationDto> myTrainingRegistrations() {
    AppUser user = authContextService.requireUser();
    return trainingRegistrationRepository.findByUserId(user.id).stream()
      .map(r -> new AccountDtos.TrainingRegistrationDto(
        r.id,
        r.session.id,
        r.session.title,
        r.session.slug,
        r.session.startDate,
        r.status,
        r.registeredAt
      ))
      .toList();
  }

  public List<AccountDtos.EventPublicDto> eventsForUser() {
    AppUser user = authContextService.requireUser();
    List<EventEntity> events = eventRepository.findByStatusIn(
      List.of(ContentStatus.PUBLISHED, ContentStatus.COMPLETED));
    return events.stream()
      .map(e -> {
        long count = eventRegistrationRepository.countActiveByEventId(e.id);
        boolean registered = eventRegistrationRepository
          .findByEventIdAndUserId(e.id, user.id)
          .map(r -> "registered".equals(r.status))
          .orElse(false);
        return new AccountDtos.EventPublicDto(
          e.id, e.title, e.slug, e.description, e.venue,
          e.startDate, e.endDate, e.startTime, e.endTime,
          e.organizer, e.contactEmail, e.registrationLink, e.status.apiValue(), e.publishedAt,
          count, registered,
          e.bannerImagePath != null ? "/api/files/event/" + e.id + "/image" : null
        );
      })
      .toList();
  }

  public void registerForEvent(UUID eventId) {
    AppUser user = authContextService.requireUser();
    EventEntity event = eventRepository.findById(eventId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Event not found"));
    if (event.status != ContentStatus.PUBLISHED) {
      throw new ResponseStatusException(BAD_REQUEST, "Registration is not open for this event");
    }
    var existing = eventRegistrationRepository.findByEventIdAndUserId(eventId, user.id);
    if (existing.isPresent()) {
      if ("registered".equals(existing.get().status)) {
        throw new ResponseStatusException(CONFLICT, "Already registered for this event");
      }
      existing.get().status = "registered";
      eventRegistrationRepository.save(existing.get());
      notifyUser(user, "Registration confirmed",
        "You're registered for \"" + event.title + "\" on " + event.startDate + ".", "/events");
      return;
    }
    EventRegistration reg = new EventRegistration();
    reg.event = event;
    reg.user = user;
    reg.status = "registered";
    eventRegistrationRepository.save(reg);
    notifyUser(user, "Registration confirmed",
      "You're registered for \"" + event.title + "\" on " + event.startDate + ".", "/events");
  }

  public void cancelEventRegistration(UUID eventId) {
    AppUser user = authContextService.requireUser();
    EventRegistration reg = eventRegistrationRepository.findByEventIdAndUserId(eventId, user.id)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Registration not found"));
    reg.status = "cancelled";
    eventRegistrationRepository.save(reg);
    String title = eventRepository.findById(eventId).map(e -> e.title).orElse("the event");
    notifyUser(user, "Registration cancelled", "Your registration for \"" + title + "\" was cancelled.", "/events");
  }

  public List<AccountDtos.EventRegistrationDto> myEventRegistrations() {
    AppUser user = authContextService.requireUser();
    return eventRegistrationRepository.findByUserId(user.id).stream()
      .map(r -> new AccountDtos.EventRegistrationDto(
        r.id,
        r.event.id,
        r.event.title,
        r.event.slug,
        r.event.startDate,
        r.status,
        r.registeredAt
      ))
      .toList();
  }

  private void notifyUser(AppUser user, String title, String message, String link) {
    Notification n = new Notification();
    n.user = user;
    n.title = title;
    n.message = message;
    n.link = link;
    notificationRepository.save(n);
  }
}
