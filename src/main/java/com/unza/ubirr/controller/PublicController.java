package com.unza.ubirr.controller;

import com.unza.ubirr.dto.AdminDtos;
import com.unza.ubirr.dto.PublicDtos;
import com.unza.ubirr.service.AdminService;
import com.unza.ubirr.service.CaptchaService;
import com.unza.ubirr.service.PublicService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {
  private final PublicService publicService;
  private final CaptchaService captchaService;
  private final AdminService adminService;

  public PublicController(PublicService publicService, CaptchaService captchaService, AdminService adminService) {
    this.publicService = publicService;
    this.captchaService = captchaService;
    this.adminService = adminService;
  }

  @GetMapping("/banners")
  public List<AdminDtos.HeroBannerDto> banners() {
    return adminService.publicBanners();
  }

  @GetMapping("/captcha-config")
  public CaptchaService.ConfigDto captchaConfig() {
    return captchaService.config();
  }

  @GetMapping("/home")
  public PublicDtos.HomeResponse home() {
    return publicService.home();
  }

  @GetMapping("/resources")
  public PublicDtos.ResourcePageDto resources(
    @RequestParam(defaultValue = "") String q,
    @RequestParam(defaultValue = "all") String type,
    @RequestParam(required = false) String category,
    @RequestParam(required = false) UUID department,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int pageSize
  ) {
    return publicService.publicResourcesPaged(q, type, category, department, page, pageSize);
  }

  @GetMapping("/departments")
  public List<PublicDtos.DepartmentDto> departments() {
    return publicService.departments();
  }

  @GetMapping("/resources/{slug}")
  public PublicDtos.ResourceDetailDto resource(@PathVariable String slug) {
    return publicService.resourceBySlug(slug);
  }

  @PostMapping("/resources/{id}/views")
  public void recordView(@PathVariable UUID id, HttpServletRequest request) {
    publicService.recordView(id, request.getRemoteAddr());
  }

  @GetMapping("/resources/{id}/reviews")
  public List<PublicDtos.ReviewDto> reviews(@PathVariable UUID id) {
    return publicService.reviewsForResource(id);
  }

  @GetMapping("/events")
  public List<PublicDtos.EventDto> events() {
    return publicService.publicEvents();
  }

  @GetMapping("/events/{slug}")
  public PublicDtos.EventDto event(@PathVariable String slug) {
    return publicService.eventBySlug(slug);
  }

  @GetMapping("/categories")
  public List<PublicDtos.CategoryDto> categories() {
    return publicService.categories();
  }

  @GetMapping("/gallery")
  public List<PublicDtos.ResourceSummaryDto> gallery() {
    return publicService.gallery();
  }

  @GetMapping("/training")
  public List<PublicDtos.TrainingSessionPublicDto> trainingSessions() {
    return publicService.publicTrainingSessions();
  }

  @GetMapping("/training/{slug}")
  public PublicDtos.TrainingSessionPublicDto trainingSession(@PathVariable String slug) {
    return publicService.trainingSessionBySlug(slug);
  }

  @GetMapping("/sites")
  public List<PublicDtos.DemonstrationSiteDto> sites() {
    return publicService.publicSites();
  }

  @GetMapping("/sites/{slug}")
  public PublicDtos.DemonstrationSiteDto site(@PathVariable String slug) {
    return publicService.siteBySlug(slug);
  }

  @GetMapping(value = "/rss", produces = MediaType.APPLICATION_XML_VALUE)
  @ResponseBody
  public String rss(HttpServletRequest request) {
    return publicService.buildRss(baseUrl(request));
  }

  @GetMapping(value = "/sitemap", produces = MediaType.APPLICATION_XML_VALUE)
  @ResponseBody
  public String sitemap(HttpServletRequest request) {
    return publicService.buildSitemap(baseUrl(request));
  }

  private String baseUrl(HttpServletRequest request) {
    String origin = request.getHeader("Origin");
    if (origin != null && !origin.isBlank()) {
      return origin;
    }
    return request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());
  }
}
