package com.unza.ubirr.controller;

import com.unza.ubirr.model.ContentFile;
import com.unza.ubirr.model.ContentItem;
import com.unza.ubirr.model.DemonstrationSite;
import com.unza.ubirr.model.EventEntity;
import com.unza.ubirr.model.HeroBanner;
import com.unza.ubirr.model.TrainingSession;
import com.unza.ubirr.repository.ContentItemRepository;
import com.unza.ubirr.repository.DemonstrationSiteRepository;
import com.unza.ubirr.repository.EventRepository;
import com.unza.ubirr.repository.HeroBannerRepository;
import com.unza.ubirr.repository.TrainingSessionRepository;
import com.unza.ubirr.service.PublicService;
import com.unza.ubirr.service.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/files")
public class FileController {
  private final PublicService publicService;
  private final StorageService storageService;
  private final ContentItemRepository contentItemRepository;
  private final DemonstrationSiteRepository demonstrationSiteRepository;
  private final HeroBannerRepository heroBannerRepository;
  private final EventRepository eventRepository;
  private final TrainingSessionRepository trainingSessionRepository;

  public FileController(
    PublicService publicService, StorageService storageService, ContentItemRepository contentItemRepository,
    DemonstrationSiteRepository demonstrationSiteRepository, HeroBannerRepository heroBannerRepository,
    EventRepository eventRepository, TrainingSessionRepository trainingSessionRepository
  ) {
    this.publicService = publicService;
    this.storageService = storageService;
    this.contentItemRepository = contentItemRepository;
    this.demonstrationSiteRepository = demonstrationSiteRepository;
    this.heroBannerRepository = heroBannerRepository;
    this.eventRepository = eventRepository;
    this.trainingSessionRepository = trainingSessionRepository;
  }

  @GetMapping("/{fileId}/preview")
  public ResponseEntity<ResourceRegion> preview(@PathVariable UUID fileId, @RequestHeader HttpHeaders requestHeaders) {
    ContentFile file = publicService.fileForPreview(fileId);
    Resource resource = storageService.load(file.filePath);
    MediaType mediaType = storageService.mediaType(file.originalFileName, file.mimeType);
    // mediaType comes from the uploader's own Content-Type/filename, so it isn't trustworthy —
    // inlining an attacker-uploaded text/html or image/svg+xml file would execute arbitrary
    // script in this origin (stored XSS) for anyone who previews it. Only render known-safe
    // (non-script-capable) types inline; everything else downloads instead.
    ContentDisposition disposition = isSafeToInline(mediaType)
      ? ContentDisposition.inline().filename(file.originalFileName).build()
      : ContentDisposition.attachment().filename(file.originalFileName).build();
    // Gated by fileForPreview's access rules, so cache per-browser only (not on a shared/CDN cache).
    return serveResource(resource, mediaType, requestHeaders, disposition, CacheControl.maxAge(Duration.ofHours(1)).cachePrivate(), file.checksum);
  }

  /**
   * Whether a MIME type is safe to render inline in the browser. Deliberately an allowlist
   * (default-deny) rather than a blocklist: image/svg+xml, text/html, text/xml and similar
   * can carry executable script, so anything not explicitly known-safe falls back to a
   * forced download instead of being guessed at.
   */
  private static boolean isSafeToInline(MediaType mediaType) {
    String type = mediaType.getType().toLowerCase(java.util.Locale.ROOT);
    String subtype = mediaType.getSubtype().toLowerCase(java.util.Locale.ROOT);
    if ("video".equals(type) || "audio".equals(type)) return true;
    if ("image".equals(type)) return !"svg+xml".equals(subtype);
    if ("application".equals(type)) return "pdf".equals(subtype);
    return false;
  }

  @GetMapping("/{fileId}/download")
  public ResponseEntity<ResourceRegion> download(@PathVariable UUID fileId, HttpServletRequest request, @RequestHeader HttpHeaders requestHeaders) {
    ContentFile file = publicService.fileForDownload(fileId, request.getRemoteAddr());
    Resource resource = storageService.load(file.filePath);
    MediaType mediaType = storageService.mediaType(file.originalFileName, file.mimeType);
    ContentDisposition disposition = ContentDisposition.attachment().filename(file.originalFileName).build();
    return serveResource(resource, mediaType, requestHeaders, disposition, CacheControl.maxAge(Duration.ofHours(1)).cachePrivate(), file.checksum);
  }

  @GetMapping("/thumbnail/{contentId}")
  public ResponseEntity<ResourceRegion> thumbnail(@PathVariable UUID contentId, @RequestHeader HttpHeaders requestHeaders) {
    ContentItem item = contentItemRepository.findById(contentId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Content not found"));
    if (item.thumbnailPath == null) {
      throw new ResponseStatusException(NOT_FOUND, "No thumbnail");
    }
    Resource resource = storageService.load(item.thumbnailPath);
    MediaType mediaType = storageService.mediaType(item.thumbnailPath, null);
    ContentDisposition disposition = ContentDisposition.inline().filename("thumbnail").build();
    // Thumbnails aren't access-gated and rarely change once generated — safe to cache publicly.
    return serveResource(resource, mediaType, requestHeaders, disposition, CacheControl.maxAge(Duration.ofHours(6)).cachePublic(), null);
  }

  /**
   * Serves a Resource with HTTP Range support (206 partial content) so video/PDF preview can
   * seek and large downloads can resume, plus Cache-Control/ETag so browsers stop re-fetching
   * unchanged files on every view.
   */
  private ResponseEntity<ResourceRegion> serveResource(
    Resource resource, MediaType mediaType, HttpHeaders requestHeaders,
    ContentDisposition disposition, CacheControl cacheControl, String etag
  ) {
    long contentLength;
    try {
      contentLength = resource.contentLength();
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on disk");
    }

    List<HttpRange> ranges = requestHeaders.getRange();
    ResourceRegion region = ranges.isEmpty()
      ? new ResourceRegion(resource, 0, contentLength)
      : ranges.get(0).toResourceRegion(resource);

    ResponseEntity.BodyBuilder builder = ResponseEntity
      .status(ranges.isEmpty() ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT)
      .contentType(mediaType)
      .header(HttpHeaders.ACCEPT_RANGES, "bytes")
      .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
      .cacheControl(cacheControl);
    if (etag != null && !etag.isBlank()) {
      builder = builder.eTag(etag);
    }
    return builder.body(region);
  }

  @GetMapping("/site/{siteId}/image")
  public ResponseEntity<Resource> siteImage(@PathVariable UUID siteId) {
    DemonstrationSite site = demonstrationSiteRepository.findById(siteId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Site not found"));
    if (site.imagePath == null) {
      throw new ResponseStatusException(NOT_FOUND, "No image");
    }
    Resource resource = storageService.load(site.imagePath);
    MediaType mediaType = storageService.mediaType(site.imagePath, null);
    ContentDisposition disposition = isSafeToInline(mediaType)
      ? ContentDisposition.inline().filename("site-image").build()
      : ContentDisposition.attachment().filename("site-image").build();
    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
      .body(resource);
  }

  @GetMapping("/event/{eventId}/image")
  public ResponseEntity<Resource> eventImage(@PathVariable UUID eventId) {
    EventEntity event = eventRepository.findById(eventId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Event not found"));
    if (event.bannerImagePath == null) throw new ResponseStatusException(NOT_FOUND, "No image");
    Resource resource = storageService.load(event.bannerImagePath);
    MediaType mediaType = storageService.mediaType(event.bannerImagePath, null);
    ContentDisposition disposition = isSafeToInline(mediaType)
      ? ContentDisposition.inline().filename("event-image").build()
      : ContentDisposition.attachment().filename("event-image").build();
    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
      .body(resource);
  }

  @GetMapping("/training/{trainingId}/image")
  public ResponseEntity<Resource> trainingImage(@PathVariable UUID trainingId) {
    TrainingSession session = trainingSessionRepository.findById(trainingId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Training session not found"));
    if (session.imagePath == null) throw new ResponseStatusException(NOT_FOUND, "No image");
    Resource resource = storageService.load(session.imagePath);
    MediaType mediaType = storageService.mediaType(session.imagePath, null);
    ContentDisposition disposition = isSafeToInline(mediaType)
      ? ContentDisposition.inline().filename("training-image").build()
      : ContentDisposition.attachment().filename("training-image").build();
    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
      .body(resource);
  }

  @GetMapping("/banner/{bannerId}/image")
  public ResponseEntity<Resource> bannerImage(@PathVariable UUID bannerId) {
    HeroBanner banner = heroBannerRepository.findById(bannerId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Banner not found"));
    if (banner.imagePath == null) throw new ResponseStatusException(NOT_FOUND, "No image");
    Resource resource = storageService.load(banner.imagePath);
    MediaType mediaType = storageService.mediaType(banner.imagePath, null);
    ContentDisposition disposition = isSafeToInline(mediaType)
      ? ContentDisposition.inline().filename("banner-image").build()
      : ContentDisposition.attachment().filename("banner-image").build();
    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
      .body(resource);
  }
}
