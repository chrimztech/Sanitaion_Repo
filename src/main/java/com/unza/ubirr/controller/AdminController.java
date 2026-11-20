package com.unza.ubirr.controller;

import com.unza.ubirr.dto.AdminDtos;
import com.unza.ubirr.dto.PublicDtos;
import com.unza.ubirr.service.AdminService;
import com.unza.ubirr.service.BackupService;
import com.unza.ubirr.service.ReportPdfService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final AdminService adminService;
  private final BackupService backupService;
  private final ReportPdfService reportPdfService;

  public AdminController(AdminService adminService, BackupService backupService, ReportPdfService reportPdfService) {
    this.adminService = adminService;
    this.backupService = backupService;
    this.reportPdfService = reportPdfService;
  }

  @GetMapping("/dashboard")
  public AdminDtos.DashboardSummaryDto dashboard() {
    return adminService.dashboard();
  }

  @GetMapping("/content")
  public List<AdminDtos.ContentEditorDto> content(@RequestParam(defaultValue = "all") String status) {
    return adminService.adminContent(status);
  }

  @PostMapping("/content")
  public AdminDtos.ContentEditorDto createContent(@Valid @RequestBody AdminDtos.SaveContentRequest request) {
    return adminService.saveContent(null, request);
  }

  @PutMapping("/content/{id}")
  public AdminDtos.ContentEditorDto updateContent(@PathVariable UUID id, @Valid @RequestBody AdminDtos.SaveContentRequest request) {
    return adminService.saveContent(id, request);
  }

  @PostMapping("/content/{id}/transition")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void transitionContent(@PathVariable UUID id, @Valid @RequestBody AdminDtos.ContentTransitionRequest request) {
    adminService.transitionContent(id, request);
  }

  @GetMapping("/content/{id}/files")
  public List<PublicDtos.ContentFileDto> contentFiles(@PathVariable UUID id) {
    return adminService.contentFiles(id);
  }

  @PostMapping("/content/{id}/files")
  public PublicDtos.ContentFileDto uploadContentFile(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
    return adminService.uploadContentFile(id, file);
  }

  @PostMapping("/content/fix-access-levels")
  public java.util.Map<String, Integer> fixDraftAccessLevels() {
    return java.util.Map.of("fixed", adminService.fixDraftAccessLevels());
  }

  @DeleteMapping("/content/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteContent(@PathVariable UUID id) {
    adminService.deleteContent(id);
  }

  @DeleteMapping("/content/files/{fileId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteContentFile(@PathVariable UUID fileId) {
    adminService.deleteContentFile(fileId);
  }

  @PostMapping("/content/{id}/thumbnail")
  public java.util.Map<String, String> uploadThumbnail(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
    String url = adminService.uploadThumbnail(id, file);
    return java.util.Map.of("url", url);
  }

  @DeleteMapping("/content/{id}/thumbnail")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteThumbnail(@PathVariable UUID id) {
    adminService.deleteThumbnail(id);
  }

  @GetMapping("/approvals")
  public List<AdminDtos.ApprovalItemDto> approvals() {
    return adminService.approvals();
  }

  @PostMapping("/approvals/{id}/action")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void approvalAction(@PathVariable UUID id, @Valid @RequestBody AdminDtos.ContentTransitionRequest request) {
    adminService.transitionContent(id, request);
  }

  @GetMapping("/events")
  public List<AdminDtos.EventAdminDto> events() {
    return adminService.events();
  }

  @PostMapping("/events")
  public AdminDtos.EventAdminDto createEvent(@Valid @RequestBody AdminDtos.SaveEventRequest request) {
    return adminService.saveEvent(null, request);
  }

  @PutMapping("/events/{id}")
  public AdminDtos.EventAdminDto updateEvent(@PathVariable UUID id, @Valid @RequestBody AdminDtos.SaveEventRequest request) {
    return adminService.saveEvent(id, request);
  }

  @PostMapping("/events/{id}/publish")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void publishEvent(@PathVariable UUID id) {
    adminService.publishEvent(id);
  }

  @DeleteMapping("/events/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteEvent(@PathVariable UUID id) {
    adminService.deleteEvent(id);
  }

  @GetMapping("/events/{id}/registrations")
  public List<AdminDtos.EventRegistrationAdminDto> eventRegistrations(@PathVariable UUID id) {
    return adminService.eventRegistrations(id);
  }

  @PutMapping("/events/registrations/{registrationId}/status")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateEventRegistrationStatus(@PathVariable UUID registrationId, @Valid @RequestBody AdminDtos.UpdateRegistrationStatusRequest request) {
    adminService.updateEventRegistrationStatus(registrationId, request.status(), request.note());
  }

  @PostMapping("/events/{id}/image")
  public java.util.Map<String, String> uploadEventImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
    return java.util.Map.of("url", adminService.uploadEventImage(id, file));
  }

  @GetMapping("/digitization")
  public List<AdminDtos.DigitizationDto> digitization() {
    return adminService.digitization();
  }

  @PostMapping("/digitization")
  public AdminDtos.DigitizationDto createDigitization(@Valid @RequestBody AdminDtos.SaveDigitizationRequest request) {
    return adminService.saveDigitization(null, request);
  }

  @PutMapping("/digitization/{id}")
  public AdminDtos.DigitizationDto updateDigitization(@PathVariable UUID id, @Valid @RequestBody AdminDtos.SaveDigitizationRequest request) {
    return adminService.saveDigitization(id, request);
  }

  @DeleteMapping("/digitization/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteDigitization(@PathVariable UUID id) {
    adminService.deleteDigitization(id);
  }

  @PatchMapping("/digitization/{id}/status")
  public AdminDtos.DigitizationDto advanceStatus(@PathVariable UUID id, @Valid @RequestBody AdminDtos.AdvanceStatusRequest request) {
    return adminService.advanceDigitizationStatus(id, request.status());
  }

  @PostMapping("/digitization/{id}/promote")
  public AdminDtos.PromoteResult promoteToLibrary(@PathVariable UUID id) {
    return adminService.promoteToLibrary(id);
  }

  @PostMapping("/digitization/{id}/scan-file")
  public AdminDtos.DigitizationDto uploadScanFile(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
    return adminService.uploadScanFile(id, file);
  }

  @GetMapping("/digitization/{id}/scan-file")
  public ResponseEntity<Resource> downloadScanFile(@PathVariable UUID id) {
    String relativePath = adminService.getScanFilePath(id);
    if (relativePath == null) return ResponseEntity.notFound().build();
    Resource resource = adminService.loadScanFile(relativePath);
    String filename = java.nio.file.Paths.get(relativePath).getFileName().toString();
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(resource);
  }

  @GetMapping("/users")
  public List<AdminDtos.UserAdminDto> users() {
    return adminService.users();
  }

  @PutMapping("/users/{id}/roles")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateRoles(@PathVariable UUID id, @RequestBody AdminDtos.UpdateUserRolesRequest request) {
    adminService.updateUserRoles(id, request);
  }

  @PutMapping("/users/{id}/status")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUserStatus(@PathVariable UUID id, @RequestBody AdminDtos.UpdateUserStatusRequest request) {
    adminService.updateUserStatus(id, request.status());
  }

  @DeleteMapping("/users/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable UUID id) {
    adminService.deleteUser(id);
  }

  @PostMapping("/users/{id}/set-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void adminResetPassword(@PathVariable UUID id, @RequestBody @jakarta.validation.Valid AdminDtos.SetPasswordRequest request) {
    adminService.adminResetPassword(id, request.password());
  }

  @GetMapping("/sessions")
  public List<AdminDtos.SessionDto> sessions() {
    return adminService.sessions();
  }

  @DeleteMapping("/sessions/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeUserSession(@PathVariable UUID userId) {
    adminService.revokeUserSession(userId);
  }

  @GetMapping("/taxonomy/{type}")
  public List<AdminDtos.TaxonomyEntryDto> taxonomy(@PathVariable String type) {
    return adminService.taxonomy(type);
  }

  @PostMapping("/taxonomy/{type}")
  public AdminDtos.TaxonomyEntryDto createTaxonomy(@PathVariable String type, @Valid @RequestBody AdminDtos.SaveTaxonomyEntryRequest request) {
    return adminService.saveTaxonomy(type, request);
  }

  @PutMapping("/taxonomy/{type}/{id}")
  public AdminDtos.TaxonomyEntryDto updateTaxonomy(@PathVariable String type, @PathVariable UUID id, @Valid @RequestBody AdminDtos.SaveTaxonomyEntryRequest request) {
    return adminService.updateTaxonomy(type, id, request);
  }

  @DeleteMapping("/taxonomy/{type}/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTaxonomy(@PathVariable String type, @PathVariable UUID id) {
    adminService.deleteTaxonomy(type, id);
  }

  @GetMapping("/settings")
  public List<AdminDtos.SystemSettingDto> settings() {
    return adminService.settings();
  }

  @PutMapping("/settings/{key}")
  public AdminDtos.SystemSettingDto updateSetting(@PathVariable String key, @Valid @RequestBody AdminDtos.UpdateSettingRequest request) {
    return adminService.updateSetting(key, request);
  }

  @GetMapping("/reports")
  public AdminDtos.ReportsDto reports() {
    return adminService.reports();
  }

  @GetMapping("/reports/access")
  public List<AdminDtos.ResourceAccessRow> resourceAccessReport() {
    return adminService.resourceAccessReport();
  }

  @GetMapping("/reports/uploads")
  public AdminDtos.UploadsTrackerDto uploadsTracker() {
    return adminService.uploadsTracker();
  }

  @GetMapping("/reports/download")
  public ResponseEntity<String> downloadReport() {
    String csv = adminService.generateReportCsv();
    String filename = "report-" + java.time.LocalDate.now() + ".csv";
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
      .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
      .body(csv);
  }

  @GetMapping("/reports/download/pdf")
  public ResponseEntity<byte[]> downloadReportPdf() {
    byte[] pdf = reportPdfService.generate();
    String filename = "report-" + java.time.LocalDate.now() + ".pdf";
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
      .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
      .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(pdf.length))
      .body(pdf);
  }

  @GetMapping("/reviews")
  public List<AdminDtos.ReviewAdminDto> reviews() {
    return adminService.reviews();
  }

  @DeleteMapping("/reviews/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteReview(@PathVariable UUID id) {
    adminService.deleteReview(id);
  }

  @GetMapping("/audit")
  public List<AdminDtos.AuditLogDto> audit(
    @RequestParam(required = false) String action,
    @RequestParam(required = false) String entityType
  ) {
    return adminService.audit(action, entityType);
  }

  @GetMapping("/backup")
  public List<AdminDtos.BackupDto> listBackups() {
    return backupService.list();
  }

  @GetMapping("/backup/location")
  public AdminDtos.BackupLocationDto backupLocation() {
    return backupService.currentLocation();
  }

  @PutMapping("/backup/location")
  public AdminDtos.BackupLocationDto updateBackupLocation(@RequestBody AdminDtos.UpdateBackupLocationRequest request) {
    return backupService.updateBackupLocation(request);
  }

  @PostMapping("/backup/database")
  public AdminDtos.BackupDto backupDatabase() {
    return backupService.createDatabaseBackup();
  }

  @PostMapping("/backup/files")
  public AdminDtos.BackupDto backupFiles() {
    return backupService.createFilesBackup();
  }

  @PostMapping("/backup/full")
  public AdminDtos.BackupDto backupFull() {
    return backupService.createFullBackup();
  }

  @GetMapping("/backup/{filename}/download")
  public ResponseEntity<Resource> downloadBackup(@PathVariable String filename) throws IOException {
    var file = backupService.resolve(filename);
    var resource = new PathResource(file);
    String contentType = filename.endsWith(".zip") ? "application/zip" : "application/octet-stream";
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
      .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(file)))
      .contentType(MediaType.parseMediaType(contentType))
      .body(resource);
  }

  @DeleteMapping("/backup/{filename}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBackup(@PathVariable String filename) {
    backupService.delete(filename);
  }

  @PostMapping("/backup/{filename}/restore")
  public AdminDtos.RestoreResultDto restoreBackup(@PathVariable String filename) {
    return backupService.restore(filename);
  }

  @GetMapping("/sites")
  public List<AdminDtos.DemonstrationSiteDto> sites() {
    return adminService.adminSites();
  }

  @PostMapping("/sites")
  public AdminDtos.DemonstrationSiteDto createSite(@Valid @RequestBody AdminDtos.SaveDemonstrationSiteRequest request) {
    return adminService.saveSite(null, request);
  }

  @PutMapping("/sites/{id}")
  public AdminDtos.DemonstrationSiteDto updateSite(@PathVariable UUID id, @Valid @RequestBody AdminDtos.SaveDemonstrationSiteRequest request) {
    return adminService.saveSite(id, request);
  }

  @DeleteMapping("/sites/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSite(@PathVariable UUID id) {
    adminService.deleteSite(id);
  }

  @GetMapping("/sites/{siteId}/monitoring")
  public List<AdminDtos.SiteMonitoringRecordDto> siteMonitoringRecords(@PathVariable UUID siteId) {
    return adminService.siteMonitoringRecords(siteId);
  }

  @PostMapping("/sites/{siteId}/monitoring")
  public AdminDtos.SiteMonitoringRecordDto createSiteMonitoringRecord(
    @PathVariable UUID siteId, @Valid @RequestBody AdminDtos.SaveSiteMonitoringRecordRequest request
  ) {
    return adminService.createSiteMonitoringRecord(siteId, request);
  }

  @PutMapping("/sites/monitoring/{recordId}")
  public AdminDtos.SiteMonitoringRecordDto updateSiteMonitoringRecord(
    @PathVariable UUID recordId, @Valid @RequestBody AdminDtos.SaveSiteMonitoringRecordRequest request
  ) {
    return adminService.updateSiteMonitoringRecord(recordId, request);
  }

  @DeleteMapping("/sites/monitoring/{recordId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSiteMonitoringRecord(@PathVariable UUID recordId) {
    adminService.deleteSiteMonitoringRecord(recordId);
  }

  @PostMapping("/sites/{id}/image")
  public java.util.Map<String, String> uploadSiteImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
    String url = adminService.uploadSiteImage(id, file);
    return java.util.Map.of("url", url);
  }

  @GetMapping("/banners")
  public List<AdminDtos.HeroBannerDto> banners() {
    return adminService.adminBanners();
  }

  @PostMapping("/banners")
  public AdminDtos.HeroBannerDto createBanner(@Valid @RequestBody AdminDtos.SaveHeroBannerRequest request) {
    return adminService.saveBanner(null, request);
  }

  @PutMapping("/banners/{id}")
  public AdminDtos.HeroBannerDto updateBanner(@PathVariable UUID id, @Valid @RequestBody AdminDtos.SaveHeroBannerRequest request) {
    return adminService.saveBanner(id, request);
  }

  @DeleteMapping("/banners/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBanner(@PathVariable UUID id) {
    adminService.deleteBanner(id);
  }

  @PostMapping("/banners/{id}/image")
  public java.util.Map<String, String> uploadBannerImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
    String url = adminService.uploadBannerImage(id, file);
    return java.util.Map.of("url", url);
  }

  @GetMapping("/training")
  public List<AdminDtos.TrainingSessionDto> trainingSessions() {
    return adminService.adminTrainingSessions();
  }

  @PostMapping("/training")
  public AdminDtos.TrainingSessionDto createTrainingSession(@Valid @RequestBody AdminDtos.SaveTrainingSessionRequest request) {
    return adminService.saveTrainingSession(null, request);
  }

  @PutMapping("/training/{id}")
  public AdminDtos.TrainingSessionDto updateTrainingSession(@PathVariable UUID id, @Valid @RequestBody AdminDtos.SaveTrainingSessionRequest request) {
    return adminService.saveTrainingSession(id, request);
  }

  @PostMapping("/training/{id}/publish")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void publishTrainingSession(@PathVariable UUID id) {
    adminService.publishTrainingSession(id);
  }

  @DeleteMapping("/training/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTrainingSession(@PathVariable UUID id) {
    adminService.deleteTrainingSession(id);
  }

  @GetMapping("/training/{id}/registrations")
  public List<AdminDtos.TrainingRegistrationAdminDto> trainingRegistrations(@PathVariable UUID id) {
    return adminService.trainingRegistrations(id);
  }

  @PutMapping("/training/registrations/{registrationId}/status")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateTrainingRegistrationStatus(@PathVariable UUID registrationId, @Valid @RequestBody AdminDtos.UpdateRegistrationStatusRequest request) {
    adminService.updateTrainingRegistrationStatus(registrationId, request.status(), request.note());
  }

  @PostMapping("/training/{id}/image")
  public java.util.Map<String, String> uploadTrainingImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
    return java.util.Map.of("url", adminService.uploadTrainingImage(id, file));
  }
}
