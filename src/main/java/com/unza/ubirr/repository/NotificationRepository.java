package com.unza.ubirr.repository;

import com.unza.ubirr.model.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
  List<Notification> findTop100ByUser_IdOrderByCreatedAtDesc(UUID userId);
  void deleteByUser_Id(UUID userId);
}
