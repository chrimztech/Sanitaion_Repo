package com.unza.ubirr.service;

import com.unza.ubirr.config.StorageProperties;
import com.unza.ubirr.repository.ContentItemRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a JPEG thumbnail for uploaded content files.
 *
 * Supported automatically:
 *   images  → resized via Thumbnailator
 *   PDFs    → first page rendered via PDFBox
 *   videos  → first frame captured via FFmpeg (if available on PATH)
 *
 * All other types return Optional.empty(); the frontend shows a styled type icon.
 */
@Service
public class ThumbnailService {

  private static final Logger log = Logger.getLogger(ThumbnailService.class.getName());

  private static final int THUMB_WIDTH = 640;
  private static final int THUMB_HEIGHT = 480;
  private static final float PDF_DPI = 96f;

  private static final List<String> IMAGE_MIMES = List.of(
    "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp", "image/tiff"
  );
  private static final List<String> VIDEO_MIMES = List.of(
    "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo",
    "video/x-matroska", "video/webm", "video/x-flv"
  );

  private final Path root;
  private final String ffmpegPath;
  private final ContentItemRepository contentItemRepository;

  public ThumbnailService(StorageProperties properties,
                          @Value("${app.ffmpeg.path:ffmpeg}") String ffmpegPath,
                          ContentItemRepository contentItemRepository) throws IOException {
    this.root = Path.of(properties.getRoot()).toAbsolutePath().normalize();
    this.ffmpegPath = ffmpegPath;
    this.contentItemRepository = contentItemRepository;
    Files.createDirectories(root.resolve("thumbnails"));
  }

  /**
   * Fire-and-forget variant used from upload request paths: thumbnail rendering (PDF page
   * rasterization, FFmpeg frame extraction) can take seconds and previously ran inline,
   * blocking the HTTP response. Runs on the async executor and attaches the result via an
   * atomic UPDATE (only if no thumbnail was set in the meantime) once generation finishes.
   */
  @Async
  public void generateAndAttachAsync(Path sourceFile, String mimeType, UUID contentId) {
    generate(sourceFile, mimeType, contentId)
      .ifPresent(path -> contentItemRepository.setThumbnailIfAbsent(contentId, path));
  }

  /**
   * Attempt to auto-generate a thumbnail for the given file.
   *
   * @param sourceFile absolute path to the stored file
   * @param mimeType   MIME type of the file (may be null)
   * @param contentId  UUID of the ContentItem (used to name the output file)
   * @return relative path to the generated thumbnail, or empty if unsupported
   */
  public Optional<String> generate(Path sourceFile, String mimeType, UUID contentId) {
    String mime = mimeType != null ? mimeType.toLowerCase() : "";
    try {
      if (IMAGE_MIMES.stream().anyMatch(mime::startsWith)) {
        return generateFromImage(sourceFile, contentId);
      }
      if (mime.equals("application/pdf")) {
        return generateFromPdf(sourceFile, contentId);
      }
      if (VIDEO_MIMES.stream().anyMatch(mime::startsWith)) {
        return generateFromVideo(sourceFile, contentId);
      }
    } catch (Exception e) {
      log.log(Level.WARNING, "Thumbnail generation failed for {0}: {1}", new Object[]{ sourceFile, e.getMessage() });
    }
    return Optional.empty();
  }

  private Optional<String> generateFromImage(Path source, UUID contentId) throws IOException {
    Path out = thumbPath(contentId);
    Files.createDirectories(out.getParent());
    Thumbnails.of(source.toFile())
      .size(THUMB_WIDTH, THUMB_HEIGHT)
      .keepAspectRatio(true)
      .outputFormat("jpg")
      .outputQuality(0.85)
      .toFile(out.toFile());
    return Optional.of(relativize(out));
  }

  private Optional<String> generateFromPdf(Path source, UUID contentId) throws IOException {
    try (PDDocument doc = Loader.loadPDF(source.toFile())) {
      if (doc.getNumberOfPages() == 0) return Optional.empty();
      PDFRenderer renderer = new PDFRenderer(doc);
      BufferedImage page = renderer.renderImageWithDPI(0, PDF_DPI);
      Path out = thumbPath(contentId);
      Files.createDirectories(out.getParent());
      Thumbnails.of(page)
        .size(THUMB_WIDTH, THUMB_HEIGHT)
        .keepAspectRatio(true)
        .outputFormat("jpg")
        .outputQuality(0.85)
        .toFile(out.toFile());
      return Optional.of(relativize(out));
    }
  }

  private Optional<String> generateFromVideo(Path source, UUID contentId) {
    try {
      Path out = thumbPath(contentId);
      Files.createDirectories(out.getParent());
      // Seek to 5 s, capture one frame; -y overwrites; -vf scales keeping aspect ratio
      ProcessBuilder pb = new ProcessBuilder(
        ffmpegPath,
        "-y",
        "-ss", "00:00:05",
        "-i", source.toAbsolutePath().toString(),
        "-frames:v", "1",
        "-vf", "scale=" + THUMB_WIDTH + ":-2",
        out.toAbsolutePath().toString()
      );
      pb.redirectErrorStream(true);
      Process proc = pb.start();
      boolean done = proc.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
      if (!done) { proc.destroyForcibly(); return Optional.empty(); }
      if (proc.exitValue() != 0 || !Files.exists(out)) return Optional.empty();
      return Optional.of(relativize(out));
    } catch (Exception e) {
      log.log(Level.FINE, "FFmpeg not available or failed: {0}", e.getMessage());
      return Optional.empty();
    }
  }

  /** Absolute path for the generated thumbnail JPEG. */
  private Path thumbPath(UUID contentId) {
    return root.resolve("thumbnails").resolve(contentId.toString()).resolve("thumb.jpg");
  }

  private String relativize(Path absolute) {
    return root.relativize(absolute).toString().replace('\\', '/');
  }

  /** Returns true if a thumbnail already exists on disk for this content item. */
  public boolean exists(UUID contentId) {
    return Files.exists(thumbPath(contentId));
  }
}
