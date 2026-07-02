package com.unza.ubirr.service;

import com.unza.ubirr.config.StorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {
  private final Path root;

  public StorageService(StorageProperties properties) throws IOException {
    this.root = Path.of(properties.getRoot()).toAbsolutePath().normalize();
    Files.createDirectories(root.resolve("content-files"));
    Files.createDirectories(root.resolve("thumbnails"));
    Files.createDirectories(root.resolve("digitization-files"));
  }

  public StoredFile storeContentFile(UUID contentId, MultipartFile file) {
    try {
      String extension = fileExtension(file.getOriginalFilename());
      String safeName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
      Path directory = root.resolve("content-files").resolve(contentId.toString());
      Files.createDirectories(directory);
      Path target = directory.resolve(safeName);
      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
      }
      return new StoredFile(
        root.relativize(target).toString().replace('\\', '/'),
        safeName,
        extension,
        file.getContentType(),
        file.getSize(),
        checksum(target)
      );
    } catch (IOException exception) {
      throw new RuntimeException("Failed to store file", exception);
    }
  }

  public StoredFile storeDigitizationFile(UUID recordId, MultipartFile file) {
    try {
      String extension = fileExtension(file.getOriginalFilename());
      String safeName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
      Path directory = root.resolve("digitization-files").resolve(recordId.toString());
      Files.createDirectories(directory);
      Path target = directory.resolve(safeName);
      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
      }
      return new StoredFile(
        root.relativize(target).toString().replace('\\', '/'),
        safeName,
        extension,
        file.getContentType(),
        file.getSize(),
        checksum(target)
      );
    } catch (IOException e) {
      throw new RuntimeException("Failed to store digitization file", e);
    }
  }

  public String storeThumbnail(UUID contentId, MultipartFile file) {
    try {
      String extension = fileExtension(file.getOriginalFilename());
      String safeName = "thumb" + (extension.isBlank() ? "" : "." + extension);
      Path directory = root.resolve("thumbnails").resolve(contentId.toString());
      Files.createDirectories(directory);
      Path target = directory.resolve(safeName);
      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
      }
      return root.relativize(target).toString().replace('\\', '/');
    } catch (IOException e) {
      throw new RuntimeException("Failed to store thumbnail", e);
    }
  }

  public Resource load(String relativePath) {
    return new FileSystemResource(root.resolve(relativePath).normalize());
  }

  public MediaType mediaType(String originalName, String explicitMimeType) {
    if (explicitMimeType != null && !explicitMimeType.isBlank()) {
      return MediaType.parseMediaType(explicitMimeType);
    }
    return MediaTypeFactory.getMediaType(originalName).orElse(MediaType.APPLICATION_OCTET_STREAM);
  }

  public void delete(String relativePath) {
    try {
      Files.deleteIfExists(root.resolve(relativePath).normalize());
    } catch (IOException exception) {
      throw new RuntimeException("Failed to delete file", exception);
    }
  }

  public record StoredFile(
    String relativePath,
    String storedFileName,
    String extension,
    String mimeType,
    long size,
    String checksum
  ) {
  }

  private String fileExtension(String name) {
    if (name == null || !name.contains(".")) {
      return "";
    }
    return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
  }

  private String checksum(Path path) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = Files.readAllBytes(path);
      return HexFormat.of().formatHex(digest.digest(bytes));
    } catch (NoSuchAlgorithmException | IOException exception) {
      throw new RuntimeException("Failed to calculate checksum", exception);
    }
  }
}
