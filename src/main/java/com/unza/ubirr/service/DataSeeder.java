package com.unza.ubirr.service;

import com.unza.ubirr.model.AppRole;
import com.unza.ubirr.model.AppUser;
import com.unza.ubirr.model.SystemSetting;
import com.unza.ubirr.model.UserRoleAssignment;
import com.unza.ubirr.repository.AppUserRepository;
import com.unza.ubirr.repository.SystemSettingRepository;
import com.unza.ubirr.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
  private final SystemSettingRepository systemSettingRepository;
  private final AppUserRepository appUserRepository;
  private final UserRoleRepository userRoleRepository;
  private final PasswordEncoder passwordEncoder;
  private final String adminEmail;
  private final String adminPassword;
  private final String adminName;

  public DataSeeder(
    SystemSettingRepository systemSettingRepository,
    AppUserRepository appUserRepository,
    UserRoleRepository userRoleRepository,
    PasswordEncoder passwordEncoder,
    @Value("${app.bootstrap.admin-email}") String adminEmail,
    @Value("${app.bootstrap.admin-password}") String adminPassword,
    @Value("${app.bootstrap.admin-name}") String adminName
  ) {
    this.systemSettingRepository = systemSettingRepository;
    this.appUserRepository = appUserRepository;
    this.userRoleRepository = userRoleRepository;
    this.passwordEncoder = passwordEncoder;
    this.adminEmail = adminEmail;
    this.adminPassword = adminPassword;
    this.adminName = adminName;
  }

  @Override
  public void run(String... args) {
    seedSettings();
    seedAdmin();
  }

  private void seedSettings() {
    seedSetting("system_name", "\"UNZA Sanitation Hub & Laboratory Repository\"", "Display name");
    seedSetting("system_abbreviation", "\"SANITATION-HUB\"", "Short system identifier");
    seedSetting("max_upload_mb", "500", "Max single-file upload in MB");
    seedSetting("default_access_level", "\"draft\"", "Default access level for new content");
    seedSetting("contact_email", "\"sanitation@unza.zm\"", "Public contact email");
    seedSetting("allow_public_registration", "true", "Allow general public to register accounts");
    seedSetting("require_approval_before_publish", "true", "Require admin approval before content goes live");
    seedSetting("download_requires_login", "true", "Authenticated users only can download files");
  }

  private void seedSetting(String key, String value, String description) {
    if (systemSettingRepository.findBySettingKey(key).isPresent()) return;
    SystemSetting setting = new SystemSetting();
    setting.settingKey = key;
    setting.settingValue = value;
    setting.description = description;
    systemSettingRepository.save(setting);
  }

  private void seedAdmin() {
    AppUser admin = appUserRepository.findByEmailIgnoreCase(adminEmail).orElseGet(() -> {
      AppUser user = new AppUser();
      user.email = adminEmail;
      user.passwordHash = passwordEncoder.encode(adminPassword);
      user.fullName = adminName;
      user.status = "active";
      user.emailVerified = true;
      return appUserRepository.save(user);
    });
    ensureRole(admin, AppRole.SUPER_ADMIN);
    ensureRole(admin, AppRole.PROJECT_ADMIN);
    ensureRole(admin, AppRole.STAFF);
  }

  private void ensureRole(AppUser user, AppRole role) {
    if (userRoleRepository.findByUser_IdAndRole(user.id, role).isPresent()) return;
    UserRoleAssignment assignment = new UserRoleAssignment();
    assignment.user = user;
    assignment.role = role;
    userRoleRepository.save(assignment);
  }
}
