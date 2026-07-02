package com.unza.ubirr.service;

import com.unza.ubirr.dto.AdminDtos;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BackupService {

  private static final DateTimeFormatter TS =
    DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.systemDefault());

  private final Path backupDir;
  private final Path storageRoot;
  private final String dbUrl;
  private final String dbUsername;
  private final String dbPassword;
  private final AuthContextService authContextService;

  public BackupService(
    @Value("${app.backup.dir:backups}") String backupDir,
    @Value("${app.storage.root:storage}") String storageRoot,
    @Value("${spring.datasource.url}") String dbUrl,
    @Value("${spring.datasource.username}") String dbUsername,
    @Value("${spring.datasource.password}") String dbPassword,
    AuthContextService authContextService
  ) throws IOException {
    this.backupDir = Path.of(backupDir).toAbsolutePath();
    this.storageRoot = Path.of(storageRoot).toAbsolutePath();
    this.dbUrl = dbUrl;
    this.dbUsername = dbUsername;
    this.dbPassword = dbPassword;
    this.authContextService = authContextService;
    Files.createDirectories(this.backupDir);
  }

  public List<AdminDtos.BackupDto> list() {
    authContextService.requireAdmin();
    try {
      if (!Files.exists(backupDir)) return List.of();
      try (var stream = Files.list(backupDir)) {
        return stream
          .filter(p -> !Files.isDirectory(p))
          .sorted(Comparator.comparingLong(p -> {
            try { return Files.getLastModifiedTime((Path) p).toMillis(); } catch (IOException e) { return 0L; }
          }).reversed())
          .map(this::toDto)
          .toList();
      }
    } catch (IOException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not list backups: " + e.getMessage());
    }
  }

  public AdminDtos.BackupDto createDatabaseBackup() {
    authContextService.requireAdmin();
    String timestamp = TS.format(Instant.now());
    Path out = backupDir.resolve("db_" + timestamp + ".sql");
    DbParams db = parseJdbcUrl(dbUrl);
    runPgDump(db, out);
    return toDto(out);
  }

  public AdminDtos.BackupDto createFilesBackup() {
    authContextService.requireAdmin();
    String timestamp = TS.format(Instant.now());
    Path out = backupDir.resolve("files_" + timestamp + ".zip");
    zipDirectory(storageRoot, out);
    return toDto(out);
  }

  public AdminDtos.BackupDto createFullBackup() {
    authContextService.requireAdmin();
    String timestamp = TS.format(Instant.now());
    Path dbFile = backupDir.resolve("_tmp_db_" + timestamp + ".sql");
    DbParams db = parseJdbcUrl(dbUrl);
    runPgDump(db, dbFile);

    Path out = backupDir.resolve("full_" + timestamp + ".zip");
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(out))) {
      addToZip(zos, dbFile, "database.sql");
      if (Files.exists(storageRoot)) {
        try (var walk = Files.walk(storageRoot)) {
          for (Path p : walk.filter(Files::isRegularFile).toList()) {
            addToZip(zos, p, "files/" + storageRoot.relativize(p));
          }
        }
      }
    } catch (IOException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Full backup failed: " + e.getMessage());
    } finally {
      try { Files.deleteIfExists(dbFile); } catch (IOException ignored) {}
    }
    return toDto(out);
  }

  public Path resolve(String filename) {
    if (!filename.matches("[a-zA-Z0-9_\\-\\.]+")) {
      throw new ResponseStatusException(BAD_REQUEST, "Invalid backup filename");
    }
    Path file = backupDir.resolve(filename).normalize();
    if (!file.startsWith(backupDir)) {
      throw new ResponseStatusException(BAD_REQUEST, "Invalid backup filename");
    }
    if (!Files.exists(file)) {
      throw new ResponseStatusException(NOT_FOUND, "Backup not found");
    }
    return file;
  }

  public void delete(String filename) {
    authContextService.requireAdmin();
    Path file = resolve(filename);
    try {
      Files.delete(file);
    } catch (IOException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not delete backup");
    }
  }

  // ── internals ────────────────────────────────────────────────────────────

  private void runPgDump(DbParams db, Path out) {
    try {
      ProcessBuilder pb = new ProcessBuilder(
        pgDumpCommand(), "-h", db.host(), "-p", db.port(), "-U", dbUsername,
        "--no-password", "-F", "p", "-f", out.toString(), db.name()
      );
      pb.environment().put("PGPASSWORD", dbPassword);
      pb.redirectErrorStream(true);
      Process proc = pb.start();
      String stderr = new String(proc.getInputStream().readAllBytes());
      int exit = proc.waitFor();
      if (exit != 0) {
        throw new ResponseStatusException(INTERNAL_SERVER_ERROR,
          "pg_dump failed (exit " + exit + "): " + stderr.trim());
      }
    } catch (IOException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR,
        "pg_dump not found. Ensure PostgreSQL bin is on PATH: " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Backup interrupted");
    }
  }

  private void zipDirectory(Path source, Path out) {
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(out))) {
      if (Files.exists(source)) {
        try (var walk = Files.walk(source)) {
          for (Path p : walk.filter(Files::isRegularFile).toList()) {
            addToZip(zos, p, source.relativize(p).toString());
          }
        }
      }
    } catch (IOException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Files backup failed: " + e.getMessage());
    }
  }

  private void addToZip(ZipOutputStream zos, Path file, Object entryName) throws IOException {
    zos.putNextEntry(new ZipEntry(entryName.toString().replace("\\", "/")));
    Files.copy(file, zos);
    zos.closeEntry();
  }

  private String pgDumpCommand() {
    // Try common Windows PostgreSQL installation paths first
    String[] candidates = {
      "pg_dump",
      "C:\\Program Files\\PostgreSQL\\18\\bin\\pg_dump.exe",
      "C:\\Program Files\\PostgreSQL\\17\\bin\\pg_dump.exe",
      "C:\\Program Files\\PostgreSQL\\16\\bin\\pg_dump.exe",
    };
    for (String cmd : candidates) {
      if (cmd.equals("pg_dump")) return cmd; // let OS resolve from PATH
      if (Path.of(cmd).toFile().exists()) return cmd;
    }
    return "pg_dump";
  }

  private DbParams parseJdbcUrl(String jdbc) {
    // jdbc:postgresql://host:port/dbname[?params]
    String s = jdbc.replaceFirst("jdbc:postgresql://", "");
    int slash = s.indexOf('/');
    String hostPort = slash > 0 ? s.substring(0, slash) : s;
    String rest = slash > 0 ? s.substring(slash + 1) : "";
    int q = rest.indexOf('?');
    String name = q > 0 ? rest.substring(0, q) : rest;
    String host = hostPort;
    String port = "5432";
    if (hostPort.contains(":")) {
      host = hostPort.substring(0, hostPort.lastIndexOf(':'));
      port = hostPort.substring(hostPort.lastIndexOf(':') + 1);
    }
    return new DbParams(host, port, name);
  }

  private AdminDtos.BackupDto toDto(Path file) {
    try {
      String name = file.getFileName().toString();
      String type = name.startsWith("db_") ? "database"
        : name.startsWith("files_") ? "files"
        : name.startsWith("full_") ? "full" : "other";
      long size = Files.exists(file) ? Files.size(file) : 0L;
      Instant created = Files.exists(file)
        ? Files.getLastModifiedTime(file).toInstant() : Instant.now();
      return new AdminDtos.BackupDto(name, type, size, created);
    } catch (IOException e) {
      return new AdminDtos.BackupDto(file.getFileName().toString(), "other", 0L, Instant.now());
    }
  }

  private record DbParams(String host, String port, String name) {}
}
