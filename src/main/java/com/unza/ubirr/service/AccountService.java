package com.unza.ubirr.service;

import com.unza.ubirr.dto.AccountDtos;
import com.unza.ubirr.model.AppUser;
import com.unza.ubirr.model.Bookmark;
import com.unza.ubirr.model.ContentItem;
import com.unza.ubirr.model.ContentStatus;
import com.unza.ubirr.model.Notification;
import com.unza.ubirr.model.ReadingList;
import com.unza.ubirr.model.ReadingListItem;
import com.unza.ubirr.model.TrainingRegistration;
import com.unza.ubirr.model.TrainingSession;
import com.unza.ubirr.model.ViewRecord;
import com.unza.ubirr.repository.AppUserRepository;
import com.unza.ubirr.repository.BookmarkRepository;
import com.unza.ubirr.repository.ContentItemRepository;
import com.unza.ubirr.repository.NotificationRepository;
import com.unza.ubirr.repository.ReadingListItemRepository;
import com.unza.ubirr.repository.ReadingListRepository;
import com.unza.ubirr.repository.TrainingRegistrationRepository;
import com.unza.ubirr.repository.TrainingSessionRepository;
import com.unza.ubirr.repository.ViewRecordRepository;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
  private final PublicService publicService;

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
    PublicService publicService
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
    this.publicService = publicService;
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
      .filter(bookmark -> publicService.canAccess(bookmark.contentItem, user))
      .map(bookmark -> new AccountDtos.BookmarkDto(bookmark.id, bookmark.createdAt, publicService.summary(bookmark.contentItem)))
      .toList();
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
      .filter(record -> record.contentItem != null && seen.add(record.contentItem.id))
      .filter(record -> publicService.canAccess(record.contentItem, user))
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
    readingListRepository.delete(ownedList(id, user.id));
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
    ReadingListItem item = readingListItemRepository.findById(itemId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reading list item not found"));
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
          count, registered
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
  }

  public void cancelTrainingRegistration(UUID sessionId) {
    AppUser user = authContextService.requireUser();
    TrainingRegistration reg = trainingRegistrationRepository.findBySessionIdAndUserId(sessionId, user.id)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Registration not found"));
    reg.status = "cancelled";
    trainingRegistrationRepository.save(reg);
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

}
