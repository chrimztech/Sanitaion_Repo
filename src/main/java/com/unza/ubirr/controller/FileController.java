package com.unza.ubirr.controller;

import com.unza.ubirr.model.ContentFile;
import com.unza.ubirr.model.ContentItem;
import com.unza.ubirr.model.DemonstrationSite;
import com.unza.ubirr.model.HeroBanner;
import com.unza.ubirr.repository.ContentItemRepository;
import com.unza.ubirr.repository.DemonstrationSiteRepository;
import com.unza.ubirr.repository.HeroBannerRepository;
import com.unza.ubirr.service.PublicService;
import com.unza.ubirr.service.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  public FileController(PublicService publicService, StorageService storageService, ContentItemRepository contentItemRepository, DemonstrationSiteRepository demonstrationSiteRepository, HeroBannerRepository heroBannerRepository) {
    this.publicService = publicService;
    this.storageService = storageService;
    this.contentItemRepository = contentItemRepository;
    this.demonstrationSiteRepository = demonstrationSiteRepository;
    this.heroBannerRepository = heroBannerRepository;
  }

  @GetMapping("/{fileId}/preview")
  public ResponseEntity<Resource> preview(@PathVariable UUID fileId) {
    ContentFile file = publicService.fileForPreview(fileId);
    Resource resource = storageService.load(file.filePath);
    MediaType mediaType = storageService.mediaType(file.originalFileName, file.mimeType);
    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(file.originalFileName).build().toString())
      .body(resource);
  }

  @GetMapping("/{fileId}/download")
  public ResponseEntity<Resource> download(@PathVariable UUID fileId, HttpServletRequest request) {
    ContentFile file = publicService.fileForDownload(fileId, request.getRemoteAddr());
    Resource resource = storageService.load(file.filePath);
    MediaType mediaType = storageService.mediaType(file.originalFileName, file.mimeType);
    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.originalFileName).build().toString())
      .body(resource);
  }

  @GetMapping("/thumbnail/{contentId}")
  public ResponseEntity<Resource> thumbnail(@PathVariable UUID contentId) {
    ContentItem item = contentItemRepository.findById(contentId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Content not found"));
    if (item.thumbnailPath == null) {
      throw new ResponseStatusException(NOT_FOUND, "No thumbnail");
    }
    Resource resource = storageService.load(item.thumbnailPath);
    MediaType mediaType = storageService.mediaType(item.thumbnailPath, null);
    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("thumbnail").build().toString())
      .body(resource);
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
    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("site-image").build().toString())
      .body(resource);
  }

  @GetMapping("/banner/{bannerId}/image")
  public ResponseEntity<Resource> bannerImage(@PathVariable UUID bannerId) {
    HeroBanner banner = heroBannerRepository.findById(bannerId)
      .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Banner not found"));
    if (banner.imagePath == null) throw new ResponseStatusException(NOT_FOUND, "No image");
    Resource resource = storageService.load(banner.imagePath);
    MediaType mediaType = storageService.mediaType(banner.imagePath, null);
    return ResponseEntity.ok()
      .contentType(mediaType)
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("banner-image").build().toString())
      .body(resource);
  }
}
