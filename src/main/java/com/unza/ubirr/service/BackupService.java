package com.unza.ubirr.service;

import com.unza.ubirr.dto.AdminDtos;
import com.unza.ubirr.model.AppUser;
import com.unza.ubirr.model.SystemSetting;
import com.unza.ubirr.repository.SystemSettingRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BackupService {

  private static final Logger log = LoggerFactory.getLogger(BackupService.class);

  private static final DateTimeFormatter TS =
    DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").withZone(ZoneId.systemDefault());

  // Stored in system_settings — lets an admin redirect backups to another drive/network path
  // at runtime, without an app restart or env var change.
  private static final String LOCATION_SETTING_KEY = "backup_storage_path";

  private final Path defaultBackupDir;
  private final Path storageRoot;
  private final String dbUrl;
  private final String dbUsername;
  private final String dbPassword;
  private final int retentionDays;
  private final AuthContextService authContextService;
  private final SystemSettingRepository systemSettingRepository;
  private final AuditService auditService;

  public BackupService(
    @Value("${app.backup.dir:backups}") String backupDir,
    @Value("${app.storage.root:storage}") String storageRoot,
    @Value("${spring.datasource.url}") String dbUrl,
    @Value("${spring.datasource.username}") String dbUsername,
    @Value("${spring.datasource.password}") String dbPassword,
    @Value("${app.backup.retention-days:30}") int retentionDays,
    AuthContextService authContextService,
    SystemSettingRepository systemSettingRepository,
    AuditService auditService
  ) throws IOException {
    this.defaultBackupDir = Path.of(backupDir).toAbsolutePath();
    this.storageRoot = Path.of(storageRoot).toAbsolutePath();
    this.dbUrl = dbUrl;
    this.dbUsername = dbUsername;
    this.dbPassword = dbPassword;
    this.retentionDays = retentionDays;
    this.authContextService = authContextService;
    this.systemSettingRepository = systemSettingRepository;
    this.auditService = auditService;
    Files.createDirectories(this.defaultBackupDir);
  }

  // Runs daily at 02:00 server time — an unattended safety net on top of the
  // admin-triggered backups, since nothing else was ensuring a backup exists
  // if nobody remembers to click the button.
  @Scheduled(cron = "0 0 2 * * *")
  public void runScheduledBackup() {
    try {
      AdminDtos.BackupDto created = createFullBackupInternal();
      log.info("Scheduled backup completed: {}", created.filename());
    } catch (Exception e) {
      log.error("Scheduled backup failed", e);
    }
    try {
      pruneExpired();
    } catch (Exception e) {
      log.error("Backup retention cleanup failed", e);
    }
  }

  private void pruneExpired() {
    if (retentionDays <= 0) return;
    Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
    try {
      Path dir = effectiveBackupDir();
      if (!Files.exists(dir)) return;
      try (var stream = Files.list(dir)) {
        for (Path p : stream.filter(f -> !Files.isDirectory(f)).toList()) {
          Instant modified = Files.getLastModifiedTime(p).toInstant();
          if (modified.isBefore(cutoff)) {
            Files.delete(p);
            log.info("Pruned expired backup: {}", p.getFileName());
          }
        }
      }
    } catch (IOException e) {
      log.error("Failed to prune expired backups", e);
    }
  }

  // ── Storage location ────────────────────────────────────────────────────

  /** The directory backups are currently read from/written to — the admin override if set, else app.backup.dir. */
  public Path effectiveBackupDir() {
    return systemSettingRepository.findBySettingKey(LOCATION_SETTING_KEY)
      .map(setting -> setting.settingValue)
      .filter(value -> value != null && !value.isBlank())
      .map(value -> Path.of(value).toAbsolutePath())
      .orElse(defaultBackupDir);
  }

  private Path ensureBackupDir() {
    Path dir = effectiveBackupDir();
    try {
      Files.createDirectories(dir);
    } catch (IOException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR,
        "Configured backup location \"" + dir + "\" is not accessible: " + e.getMessage());
    }
    return dir;
  }

  public AdminDtos.BackupLocationDto currentLocation() {
    authContextService.requireAdmin();
    Path dir = effectiveBackupDir();
    boolean writable = Files.isDirectory(dir) && Files.isWritable(dir);
    return new AdminDtos.BackupLocationDto(dir.toString(), dir.equals(defaultBackupDir), writable);
  }

  public AdminDtos.BackupLocationDto updateBackupLocation(AdminDtos.UpdateBackupLocationRequest request) {
    AppUser user = requireAdminUser();
    String trimmed = request.path() == null ? "" : request.path().trim();

    if (trimmed.isEmpty()) {
      systemSettingRepository.findBySettingKey(LOCATION_SETTING_KEY)
        .ifPresent(setting -> {
          auditService.log(user, "backup.location.reset", "system_settings", LOCATION_SETTING_KEY, setting.settingValue, null);
          systemSettingRepository.delete(setting);
        });
      return currentLocation();
    }

    Path candidate = Path.of(trimmed).toAbsolutePath();
    validateWritable(candidate);

    SystemSetting setting = systemSettingRepository.findBySettingKey(LOCATION_SETTING_KEY)
      .orElseGet(() -> {
        SystemSetting created = new SystemSetting();
        created.settingKey = LOCATION_SETTING_KEY;
        created.description = "Filesystem path where backups are written (blank = default app.backup.dir)";
        return created;
      });
    String old = setting.settingValue;
    setting.settingValue = candidate.toString();
    setting.updatedBy = user;
    systemSettingRepository.save(setting);
    auditService.log(user, "backup.location.update", "system_settings", LOCATION_SETTING_KEY, old, candidate.toString());
    return currentLocation();
  }

  /** Proves the path is actually usable (exists or creatable, and writable) before we accept it. */
  private void validateWritable(Path dir) {
    try {
      Files.createDirectories(dir);
      Path probe = dir.resolve(".ubirr-write-test-" + Instant.now().toEpochMilli());
      Files.writeString(probe, "ok");
      Files.delete(probe);
    } catch (Exception e) {
      throw new ResponseStatusException(BAD_REQUEST,
        "Cannot use \"" + dir + "\" as the backup location — it must be a path this server can create and write to "
          + "(e.g. a local path, a mounted drive, or a mapped network share). Detail: " + e.getMessage());
    }
  }

  // ── Create ───────────────────────────────────────────────────────────────

  public List<AdminDtos.BackupDto> list() {
    authContextService.requireAdmin();
    try {
      Path dir = effectiveBackupDir();
      if (!Files.exists(dir)) return List.of();
      try (var stream = Files.list(dir)) {
        return stream
          .filter(p -> !Files.isDirectory(p))
          .filter(p -> !p.getFileName().toString().startsWith("."))
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
    AppUser user = requireAdminUser();
    AdminDtos.BackupDto dto = createDatabaseBackupInternal();
    auditService.log(user, "backup.create.database", "backup", dto.filename());
    return dto;
  }

  public AdminDtos.BackupDto createFilesBackup() {
    AppUser user = requireAdminUser();
    AdminDtos.BackupDto dto = createFilesBackupInternal();
    auditService.log(user, "backup.create.files", "backup", dto.filename());
    return dto;
  }

  public AdminDtos.BackupDto createFullBackup() {
    AppUser user = requireAdminUser();
    AdminDtos.BackupDto dto = createFullBackupInternal();
    auditService.log(user, "backup.create.full", "backup", dto.filename());
    return dto;
  }

  private AdminDtos.BackupDto createDatabaseBackupInternal() {
    String timestamp = TS.format(Instant.now());
    Path out = ensureBackupDir().resolve("db_" + timestamp + ".sql");
    DbParams db = parseJdbcUrl(dbUrl);
    runPgDump(db, out);
    return toDto(out);
  }

  private AdminDtos.BackupDto createFilesBackupInternal() {
    String timestamp = TS.format(Instant.now());
    Path out = ensureBackupDir().resolve("files_" + timestamp + ".zip");
    zipDirectory(storageRoot, out);
    return toDto(out);
  }

  private AdminDtos.BackupDto createFullBackupInternal() {
    Path dir = ensureBackupDir();
    String timestamp = TS.format(Instant.now());
    Path dbFile = dir.resolve("_tmp_db_" + timestamp + ".sql");
    DbParams db = parseJdbcUrl(dbUrl);
    runPgDump(db, dbFile);

    Path out = dir.resolve("full_" + timestamp + ".zip");
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
    Path dir = effectiveBackupDir();
    Path file = dir.resolve(filename).normalize();
    if (!file.startsWith(dir)) {
      throw new ResponseStatusException(BAD_REQUEST, "Invalid backup filename");
    }
    if (!Files.exists(file)) {
      throw new ResponseStatusException(NOT_FOUND, "Backup not found");
    }
    return file;
  }

  public void delete(String filename) {
    AppUser user = requireAdminUser();
    Path file = resolve(filename);
    try {
      Files.delete(file);
      auditService.log(user, "backup.delete", "backup", filename);
    } catch (IOException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not delete backup");
    }
  }

  // ── Restore ──────────────────────────────────────────────────────────────

  /**
   * Restores a previously-created backup. Database restores run `psql` with ON_ERROR_STOP=1
   * against a dump produced with --clean --if-exists, so tables are dropped and recreated
   * cleanly rather than colliding with existing data. Files restores extract into the storage
   * root, overwriting any file at the same relative path. This is destructive and irreversible
   * — the controller requires admin auth and the frontend requires explicit confirmation.
   */
  public AdminDtos.RestoreResultDto restore(String filename) {
    AppUser user = requireAdminUser();
    Path file = resolve(filename);
    String name = file.getFileName().toString();
    boolean databaseRestored = false;
    boolean filesRestored = false;

    try {
      if (name.startsWith("db_")) {
        restoreDatabaseFromSqlFile(file);
        databaseRestored = true;
      } else if (name.startsWith("files_")) {
        restoreFilesFromZip(file, null);
        filesRestored = true;
      } else if (name.startsWith("full_")) {
        Path tmpSql = Files.createTempFile("ubirr-restore-db-", ".sql");
        try {
          extractZipEntryToFile(file, "database.sql", tmpSql);
          restoreDatabaseFromSqlFile(tmpSql);
          databaseRestored = true;
        } finally {
          try { Files.deleteIfExists(tmpSql); } catch (IOException ignored) {}
        }
        restoreFilesFromZip(file, "files/");
        filesRestored = true;
      } else {
        throw new ResponseStatusException(BAD_REQUEST, "This file isn't a recognized backup archive");
      }
    } catch (ResponseStatusException e) {
      auditService.log(user, "backup.restore.failed", "backup", name, null, e.getReason());
      throw e;
    } catch (Exception e) {
      auditService.log(user, "backup.restore.failed", "backup", name, null, e.getMessage());
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Restore failed: " + e.getMessage());
    }

    auditService.log(user, "backup.restore", "backup", name);
    String message = "Restore completed:"
      + (databaseRestored ? " database restored" : "")
      + (databaseRestored && filesRestored ? " and" : "")
      + (filesRestored ? " files restored" : "")
      + ".";
    return new AdminDtos.RestoreResultDto(name, databaseRestored, filesRestored, message);
  }

  private void restoreDatabaseFromSqlFile(Path sqlFile) {
    DbParams db = parseJdbcUrl(dbUrl);
    try {
      ProcessBuilder pb = new ProcessBuilder(
        psqlCommand(), "-h", db.host(), "-p", db.port(), "-U", dbUsername,
        "--no-password", "-v", "ON_ERROR_STOP=1", "-f", sqlFile.toString(), db.name()
      );
      pb.environment().put("PGPASSWORD", dbPassword);
      pb.redirectErrorStream(true);
      Process proc = pb.start();
      String output = new String(proc.getInputStream().readAllBytes());
      int exit = proc.waitFor();
      if (exit != 0) {
        throw new ResponseStatusException(INTERNAL_SERVER_ERROR,
          "Database restore failed (exit " + exit + "): " + lastLines(output, 20));
      }
    } catch (IOException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR,
        "psql not found. Ensure PostgreSQL bin is on PATH: " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Restore interrupted");
    }
  }

  /**
   * Extracts entries from a backup zip into the storage root, overwriting matching files.
   * When onlyPrefix is set (used for full_*.zip), only entries under files/ are extracted,
   * with that prefix stripped so paths land back at the storage root correctly.
   */
  private void restoreFilesFromZip(Path zipFile, String onlyPrefix) {
    try (var zis = new ZipInputStream(Files.newInputStream(zipFile))) {
      Files.createDirectories(storageRoot);
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (entry.isDirectory()) continue;
        String entryName = entry.getName();
        if (onlyPrefix != null) {
          if (!entryName.startsWith(onlyPrefix)) continue;
          entryName = entryName.substring(onlyPrefix.length());
        }
        if (entryName.isBlank()) continue;
        Path target = storageRoot.resolve(entryName).normalize();
        if (!target.startsWith(storageRoot)) continue; // zip-slip guard
        Files.createDirectories(target.getParent());
        Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Files restore failed: " + e.getMessage());
    }
  }

  private void extractZipEntryToFile(Path zipFile, String entryName, Path outFile) throws IOException {
    try (var zis = new ZipInputStream(Files.newInputStream(zipFile))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (entry.getName().equals(entryName)) {
          Files.copy(zis, outFile, StandardCopyOption.REPLACE_EXISTING);
          return;
        }
      }
    }
    throw new ResponseStatusException(BAD_REQUEST, "Backup archive is missing " + entryName);
  }

  private String lastLines(String text, int maxLines) {
    String[] lines = text.strip().split("\\R");
    if (lines.length <= maxLines) return text.strip();
    return String.join("\n", java.util.Arrays.copyOfRange(lines, lines.length - maxLines, lines.length));
  }

  // ── internals ────────────────────────────────────────────────────────────

  private AppUser requireAdminUser() {
    AppUser user = authContextService.requireUser();
    if (!authContextService.isAdmin(user)) {
      throw new ResponseStatusException(FORBIDDEN, "Administrator access required");
    }
    return user;
  }

  private void runPgDump(DbParams db, Path out) {
    try {
      ProcessBuilder pb = new ProcessBuilder(
        pgDumpCommand(), "-h", db.host(), "-p", db.port(), "-U", dbUsername,
        // --clean --if-exists: emits DROP ... IF EXISTS before each CREATE, so restoring
        // this dump later onto an already-populated database works cleanly instead of
        // failing with "relation already exists" / duplicate-key errors.
        "--no-password", "--clean", "--if-exists", "-F", "p", "-f", out.toString(), db.name()
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
    return resolvePgBinary("pg_dump", "pg_dump.exe");
  }

  private String psqlCommand() {
    return resolvePgBinary("psql", "psql.exe");
  }

  // Try common Windows PostgreSQL installation paths first, then fall back to PATH resolution.
  private String resolvePgBinary(String unixName, String windowsExeName) {
    String[] versions = { "18", "17", "16" };
    for (String version : versions) {
      Path candidate = Path.of("C:\\Program Files\\PostgreSQL\\" + version + "\\bin\\" + windowsExeName);
      if (Files.exists(candidate)) return candidate.toString();
    }
    return unixName;
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
