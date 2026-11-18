package com.unza.ubirr.repository;

import com.unza.ubirr.model.ContentStatus;
import com.unza.ubirr.model.TrainingSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, UUID> {
  Optional<TrainingSession> findBySlug(String slug);
  boolean existsBySlug(String slug);
  List<TrainingSession> findByStatus(ContentStatus status);
  List<TrainingSession> findByStatusIn(List<ContentStatus> statuses);
  long countByStatusIn(List<ContentStatus> statuses);
}
