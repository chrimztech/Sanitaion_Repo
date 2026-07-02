package com.unza.ubirr.repository;

import com.unza.ubirr.model.SystemSetting;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, UUID> {
  Optional<SystemSetting> findBySettingKey(String settingKey);
}
