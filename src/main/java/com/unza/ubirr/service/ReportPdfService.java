package com.unza.ubirr.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.unza.ubirr.model.*;
import com.unza.ubirr.repository.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportPdfService {

  private static final Color GREEN_DARK  = new Color(10, 52, 26);
  private static final Color GREEN_MID   = new Color(22, 101, 52);
  private static final Color GREEN_LIGHT = new Color(220, 252, 231);
  private static final Color GREY_HEADER = new Color(243, 244, 246);
  private static final Color GREY_TEXT   = new Color(107, 114, 128);
  private static final Color WHITE       = Color.WHITE;

  private static final DateTimeFormatter DATE_FMT =
      DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());
  private static final DateTimeFormatter DATETIME_FMT =
      DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(ZoneId.systemDefault());

  private final AuthContextService authContextService;
  private final ContentItemRepository contentItemRepository;
  private final AppUserRepository appUserRepository;
  private final EventRepository eventRepository;
  private final TrainingSessionRepository trainingSessionRepository;
  private final TrainingRegistrationRepository trainingRegistrationRepository;
  private final DemonstrationSiteRepository demonstrationSiteRepository;
  private final ViewRecordRepository viewRecordRepository;
  private final DownloadRecordRepository downloadRecordRepository;
  private final ContentFileRepository contentFileRepository;
  private final DigitizationRecordRepository digitizationRecordRepository;

  public ReportPdfService(
      AuthContextService authContextService,
      ContentItemRepository contentItemRepository,
      AppUserRepository appUserRepository,
      EventRepository eventRepository,
      TrainingSessionRepository trainingSessionRepository,
      TrainingRegistrationRepository trainingRegistrationRepository,
      DemonstrationSiteRepository demonstrationSiteRepository,
      ViewRecordRepository viewRecordRepository,
      DownloadRecordRepository downloadRecordRepository,
      ContentFileRepository contentFileRepository,
      DigitizationRecordRepository digitizationRecordRepository
  ) {
    this.authContextService = authContextService;
    this.contentItemRepository = contentItemRepository;
    this.appUserRepository = appUserRepository;
    this.eventRepository = eventRepository;
    this.trainingSessionRepository = trainingSessionRepository;
    this.trainingRegistrationRepository = trainingRegistrationRepository;
    this.demonstrationSiteRepository = demonstrationSiteRepository;
    this.viewRecordRepository = viewRecordRepository;
    this.downloadRecordRepository = downloadRecordRepository;
    this.contentFileRepository = contentFileRepository;
    this.digitizationRecordRepository = digitizationRecordRepository;
  }

  public byte[] generate() {
    authContextService.requireAdmin();

    // Pre-fetch aggregates
    List<ContentItem> items = contentItemRepository.findAll();
    List<ContentFile> files = contentFileRepository.findAll();
    long totalBytes = files.stream().mapToLong(f -> f.fileSize != null ? f.fileSize : 0L).sum();

    Map<UUID, Long> viewsByItem = new HashMap<>();
    for (Object[] row : viewRecordRepository.countGroupedByContentItem()) {
      viewsByItem.put((UUID) row[0], (Long) row[1]);
    }
    Map<UUID, Long> downloadsByItem = new HashMap<>();
    for (Object[] row : downloadRecordRepository.countGroupedByContentItem()) {
      downloadsByItem.put((UUID) row[0], (Long) row[1]);
    }

    try (var out = new ByteArrayOutputStream()) {
      Document doc = new Document(PageSize.A4.rotate(), 36, 36, 60, 36);
      PdfWriter writer = PdfWriter.getInstance(doc, out);
      writer.setPageEvent(new HeaderFooter());
      doc.open();

      // ── Cover section ──────────────────────────────────────────────────────
      Font titleFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, GREEN_DARK);
      Font subFont     = FontFactory.getFont(FontFactory.HELVETICA, 10, GREY_TEXT);
      Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, WHITE);
      Font labelFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, GREY_TEXT);
      Font valueFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, GREEN_DARK);
      Font bodyFont    = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
      Font bodyBold    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
      Font hdrFont     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, WHITE);

      Paragraph title = new Paragraph("UNZA Sanitation Hub — System Report", titleFont);
      title.setAlignment(Element.ALIGN_LEFT);
      doc.add(title);

      Paragraph generated = new Paragraph(
          "Generated " + DATETIME_FMT.format(java.time.Instant.now()), subFont);
      generated.setSpacingAfter(16);
      doc.add(generated);

      // ── Summary cards ──────────────────────────────────────────────────────
      addSectionHeader(doc, "Summary", sectionFont);

      long published = items.stream().filter(i -> i.status == ContentStatus.PUBLISHED).count();
      long pending   = items.stream().filter(i -> i.status == ContentStatus.SUBMITTED).count();
      long drafts    = items.stream().filter(i -> i.status == ContentStatus.DRAFT).count();
      long archived  = items.stream().filter(i -> i.status == ContentStatus.ARCHIVED).count();
      long totalViews = viewRecordRepository.count();
      long totalDl    = downloadRecordRepository.count();

      PdfPTable cards = new PdfPTable(6);
      cards.setWidthPercentage(100);
      cards.setSpacingAfter(14);
      addCard(cards, "Total Resources", String.valueOf(items.size()), valueFont, labelFont);
      addCard(cards, "Published", String.valueOf(published), valueFont, labelFont);
      addCard(cards, "Pending Approval", String.valueOf(pending), valueFont, labelFont);
      addCard(cards, "Registered Users", String.valueOf(appUserRepository.count()), valueFont, labelFont);
      addCard(cards, "Total Views", String.valueOf(totalViews), valueFont, labelFont);
      addCard(cards, "Total Downloads", String.valueOf(totalDl), valueFont, labelFont);
      doc.add(cards);

      PdfPTable cards2 = new PdfPTable(5);
      cards2.setWidthPercentage(100);
      cards2.setSpacingAfter(16);
      addCard(cards2, "Drafts", String.valueOf(drafts), valueFont, labelFont);
      addCard(cards2, "Archived", String.valueOf(archived), valueFont, labelFont);
      addCard(cards2, "Events", String.valueOf(eventRepository.count()), valueFont, labelFont);
      addCard(cards2, "Training Sessions", String.valueOf(trainingSessionRepository.count()), valueFont, labelFont);
      addCard(cards2, "Files Stored (" + formatBytes(totalBytes) + ")", String.valueOf(files.size()), valueFont, labelFont);
      doc.add(cards2);

      // ── Content by type ────────────────────────────────────────────────────
      addSectionHeader(doc, "Content by Type", sectionFont);
      Map<String, Long> byType = new java.util.LinkedHashMap<>();
      for (ContentItem item : items) byType.merge(item.contentType, 1L, Long::sum);

      PdfPTable typeTable = new PdfPTable(2);
      typeTable.setWidthPercentage(50);
      typeTable.setHorizontalAlignment(Element.ALIGN_LEFT);
      typeTable.setSpacingAfter(14);
      addTableHeader(typeTable, hdrFont, "Content Type", "Count");
      boolean alt = false;
      for (var entry : byType.entrySet()) {
        addRow(typeTable, bodyFont, alt, humanType(entry.getKey()), String.valueOf(entry.getValue()));
        alt = !alt;
      }
      doc.add(typeTable);

      // ── Resources ──────────────────────────────────────────────────────────
      addSectionHeader(doc, "Resources", sectionFont);
      PdfPTable resTable = new PdfPTable(new float[]{3.5f, 2f, 1.2f, 1.2f, 1.5f, 1f, 1f});
      resTable.setWidthPercentage(100);
      resTable.setSpacingAfter(14);
      addTableHeader(resTable, hdrFont, "Title", "Type", "Status", "Access", "Author", "Views", "Downloads");
      alt = false;
      for (ContentItem item : items.stream()
          .sorted(java.util.Comparator.comparing((ContentItem i) -> i.createdAt).reversed()).toList()) {
        addRow(resTable, bodyFont, alt,
            item.title,
            humanType(item.contentType),
            capitalize(item.status.apiValue()),
            capitalize(item.accessLevel.apiValue()),
            item.author != null && item.author.fullName != null ? item.author.fullName : "—",
            String.valueOf(viewsByItem.getOrDefault(item.id, 0L)),
            String.valueOf(downloadsByItem.getOrDefault(item.id, 0L))
        );
        alt = !alt;
      }
      doc.add(resTable);

      // ── Users ──────────────────────────────────────────────────────────────
      doc.newPage();
      addSectionHeader(doc, "Registered Users", sectionFont);
      PdfPTable userTable = new PdfPTable(new float[]{2.5f, 3f, 2f, 1.2f, 1.5f, 2f});
      userTable.setWidthPercentage(100);
      userTable.setSpacingAfter(14);
      addTableHeader(userTable, hdrFont, "Full Name", "Email", "Roles", "Status", "Auth Type", "Registered");
      alt = false;
      for (AppUser u : appUserRepository.findAll(Sort.by("createdAt"))) {
        String roles = u.roles.stream()
            .map(r -> r.role.name())
            .collect(java.util.stream.Collectors.joining(", "));
        addRow(userTable, bodyFont, alt,
            u.fullName != null ? u.fullName : "—",
            u.email,
            roles.isEmpty() ? "—" : roles,
            capitalize(u.status),
            u.authType != null ? u.authType.name() : "LOCAL",
            u.createdAt != null ? DATE_FMT.format(u.createdAt) : "—"
        );
        alt = !alt;
      }
      doc.add(userTable);

      // ── Events ─────────────────────────────────────────────────────────────
      addSectionHeader(doc, "Events", sectionFont);
      PdfPTable evTable = new PdfPTable(new float[]{3f, 1.5f, 1.5f, 1.5f, 2f, 2f});
      evTable.setWidthPercentage(100);
      evTable.setSpacingAfter(14);
      addTableHeader(evTable, hdrFont, "Title", "Status", "Start Date", "End Date", "Venue", "Organizer");
      alt = false;
      for (EventEntity ev : eventRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate"))) {
        addRow(evTable, bodyFont, alt,
            ev.title,
            capitalize(ev.status.apiValue()),
            ev.startDate != null ? ev.startDate.toString() : "—",
            ev.endDate   != null ? ev.endDate.toString()   : "—",
            ev.venue     != null ? ev.venue     : "—",
            ev.organizer != null ? ev.organizer : "—"
        );
        alt = !alt;
      }
      doc.add(evTable);

      // ── Training sessions ──────────────────────────────────────────────────
      addSectionHeader(doc, "Training Sessions", sectionFont);
      PdfPTable trainTable = new PdfPTable(new float[]{3f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.2f});
      trainTable.setWidthPercentage(100);
      trainTable.setSpacingAfter(14);
      addTableHeader(trainTable, hdrFont, "Title", "Mode", "Status", "Start Date", "Venue", "Capacity", "Registrations");
      alt = false;
      for (TrainingSession s : trainingSessionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))) {
        long regCount = trainingRegistrationRepository.countActiveBySessionId(s.id);
        addRow(trainTable, bodyFont, alt,
            s.title,
            s.mode   != null ? capitalize(s.mode) : "—",
            capitalize(s.status.apiValue()),
            s.startDate != null ? s.startDate.toString() : "—",
            s.venue     != null ? s.venue : "—",
            s.capacity  != null ? String.valueOf(s.capacity) : "Unlimited",
            String.valueOf(regCount)
        );
        alt = !alt;
      }
      doc.add(trainTable);

      // ── Demonstration sites ────────────────────────────────────────────────
      addSectionHeader(doc, "Demonstration Sites", sectionFont);
      PdfPTable siteTable = new PdfPTable(new float[]{2.5f, 1.5f, 1.5f, 1.5f, 2f, 1.5f});
      siteTable.setWidthPercentage(100);
      siteTable.setSpacingAfter(14);
      addTableHeader(siteTable, hdrFont, "Name", "Type", "Status", "District", "Location", "Since");
      alt = false;
      for (DemonstrationSite site : demonstrationSiteRepository.findAll(Sort.by("name"))) {
        addRow(siteTable, bodyFont, alt,
            site.name,
            site.siteType          != null ? site.siteType          : "—",
            site.operationalStatus != null ? capitalize(site.operationalStatus) : "—",
            site.district          != null ? site.district          : "—",
            site.location          != null ? site.location          : "—",
            site.operationalSince  != null ? site.operationalSince.toString() : "—"
        );
        alt = !alt;
      }
      doc.add(siteTable);

      doc.close();
      return out.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate PDF report", e);
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private void addSectionHeader(Document doc, String text, Font font) throws DocumentException {
    PdfPTable banner = new PdfPTable(1);
    banner.setWidthPercentage(100);
    banner.setSpacingBefore(10);
    banner.setSpacingAfter(6);
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setBackgroundColor(GREEN_MID);
    cell.setPadding(6);
    cell.setBorder(Rectangle.NO_BORDER);
    banner.addCell(cell);
    doc.add(banner);
  }

  private void addCard(PdfPTable table, String label, String value, Font valueFont, Font labelFont) {
    PdfPCell cell = new PdfPCell();
    cell.setBorderColor(new Color(229, 231, 235));
    cell.setPadding(8);
    cell.setBackgroundColor(GREY_HEADER);
    Paragraph p = new Paragraph();
    p.add(new Chunk(value + "\n", valueFont));
    p.add(new Chunk(label, labelFont));
    cell.addElement(p);
    table.addCell(cell);
  }

  private void addTableHeader(PdfPTable table, Font font, String... headers) {
    for (String h : headers) {
      PdfPCell cell = new PdfPCell(new Phrase(h, font));
      cell.setBackgroundColor(GREEN_MID);
      cell.setPadding(5);
      cell.setBorderColor(new Color(255, 255, 255));
      cell.setHorizontalAlignment(Element.ALIGN_LEFT);
      table.addCell(cell);
    }
  }

  private void addRow(PdfPTable table, Font font, boolean alt, String... values) {
    Color bg = alt ? GREY_HEADER : WHITE;
    for (String v : values) {
      PdfPCell cell = new PdfPCell(new Phrase(v != null ? v : "—", font));
      cell.setBackgroundColor(bg);
      cell.setPadding(4);
      cell.setBorderColor(new Color(229, 231, 235));
      table.addCell(cell);
    }
  }

  private String capitalize(String s) {
    if (s == null || s.isEmpty()) return s;
    return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
  }

  private String humanType(String type) {
    if (type == null) return "";
    return type.replace("_", " ").substring(0, 1).toUpperCase()
        + type.replace("_", " ").substring(1);
  }

  private String formatBytes(long bytes) {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
    if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / 1024.0 / 1024);
    return String.format("%.2f GB", bytes / 1024.0 / 1024 / 1024);
  }

  // ── Page header/footer via PdfPageEventHelper ──────────────────────────

  private static class HeaderFooter extends PdfPageEventHelper {
    private final Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 7, GREY_TEXT);

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      PdfContentByte cb = writer.getDirectContent();
      // Top green bar
      cb.setColorFill(GREEN_DARK);
      cb.rectangle(document.left(), document.top() + 6, document.right() - document.left(), 4);
      cb.fill();
      // Footer line
      cb.setColorStroke(new Color(229, 231, 235));
      cb.moveTo(document.left(), document.bottom() - 8);
      cb.lineTo(document.right(), document.bottom() - 8);
      cb.stroke();
      // Footer text
      ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
          new Phrase("UNZA Sanitation Hub — Confidential", footerFont),
          document.left(), document.bottom() - 18, 0);
      ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
          new Phrase("Page " + writer.getPageNumber(), footerFont),
          document.right(), document.bottom() - 18, 0);
    }
  }
}
