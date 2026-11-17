package com.unza.ubirr.controller;

import com.unza.ubirr.dto.AccountDtos;
import com.unza.ubirr.dto.PublicDtos;
import com.unza.ubirr.service.AccountService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {
  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping("/profile")
  public AccountDtos.ProfileDto profile() {
    return accountService.profile();
  }

  @PutMapping("/profile")
  public AccountDtos.ProfileDto updateProfile(@Valid @RequestBody AccountDtos.UpdateProfileRequest request) {
    return accountService.updateProfile(request);
  }

  @GetMapping("/bookmarks")
  public List<AccountDtos.BookmarkDto> bookmarks() {
    return accountService.bookmarks();
  }

  @GetMapping("/bookmarks/{contentId}/status")
  public AccountDtos.BookmarkStatusDto bookmarkStatus(@PathVariable UUID contentId) {
    return accountService.bookmarkStatus(contentId);
  }

  @PostMapping("/bookmarks/{contentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void addBookmark(@PathVariable UUID contentId) {
    accountService.addBookmark(contentId);
  }

  @DeleteMapping("/bookmarks/{contentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeBookmark(@PathVariable UUID contentId) {
    accountService.removeBookmark(contentId);
  }

  @PostMapping("/reviews/{contentId}")
  public PublicDtos.ReviewDto submitReview(@PathVariable UUID contentId, @Valid @RequestBody AccountDtos.CreateReviewRequest request) {
    return accountService.upsertReview(contentId, request);
  }

  @DeleteMapping("/reviews/{contentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteReview(@PathVariable UUID contentId) {
    accountService.deleteOwnReview(contentId);
  }

  @GetMapping("/recent-views")
  public List<AccountDtos.RecentViewDto> recentViews() {
    return accountService.recentViews();
  }

  @GetMapping("/notifications")
  public List<AccountDtos.NotificationDto> notifications() {
    return accountService.notifications();
  }

  @PutMapping("/notifications/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markRead(@RequestBody AccountDtos.MarkNotificationsReadRequest request) {
    accountService.markNotificationsRead(request);
  }

  @GetMapping("/reading-lists")
  public List<AccountDtos.ReadingListDto> readingLists() {
    return accountService.readingLists();
  }

  @PostMapping("/reading-lists")
  public AccountDtos.ReadingListDto createReadingList(@Valid @RequestBody AccountDtos.SaveReadingListRequest request) {
    return accountService.createReadingList(request);
  }

  @PutMapping("/reading-lists/{id}")
  public AccountDtos.ReadingListDto updateReadingList(@PathVariable UUID id, @Valid @RequestBody AccountDtos.SaveReadingListRequest request) {
    return accountService.updateReadingList(id, request);
  }

  @DeleteMapping("/reading-lists/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteReadingList(@PathVariable UUID id) {
    accountService.deleteReadingList(id);
  }

  @PostMapping("/reading-lists/{id}/items")
  public AccountDtos.ReadingListDto addReadingListItem(@PathVariable UUID id, @RequestBody AccountDtos.SaveReadingListItemRequest request) {
    return accountService.addReadingListItem(id, request);
  }

  @DeleteMapping("/reading-lists/items/{itemId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteReadingListItem(@PathVariable UUID itemId) {
    accountService.removeReadingListItem(itemId);
  }

  @GetMapping("/training")
  public List<AccountDtos.TrainingSessionPublicDto> trainingSessions() {
    return accountService.trainingSessionsForUser();
  }

  @PostMapping("/training/{sessionId}/register")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void registerForTraining(@PathVariable UUID sessionId) {
    accountService.registerForTraining(sessionId);
  }

  @DeleteMapping("/training/{sessionId}/register")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancelTrainingRegistration(@PathVariable UUID sessionId) {
    accountService.cancelTrainingRegistration(sessionId);
  }

  @GetMapping("/training/my-registrations")
  public List<AccountDtos.TrainingRegistrationDto> myTrainingRegistrations() {
    return accountService.myTrainingRegistrations();
  }

  @GetMapping("/events")
  public List<AccountDtos.EventPublicDto> events() {
    return accountService.eventsForUser();
  }

  @PostMapping("/events/{eventId}/register")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void registerForEvent(@PathVariable UUID eventId) {
    accountService.registerForEvent(eventId);
  }

  @DeleteMapping("/events/{eventId}/register")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancelEventRegistration(@PathVariable UUID eventId) {
    accountService.cancelEventRegistration(eventId);
  }

  @GetMapping("/events/my-registrations")
  public List<AccountDtos.EventRegistrationDto> myEventRegistrations() {
    return accountService.myEventRegistrations();
  }
}
