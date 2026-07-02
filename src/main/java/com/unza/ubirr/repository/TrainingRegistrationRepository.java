package com.unza.ubirr.repository;

import com.unza.ubirr.model.TrainingRegistration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainingRegistrationRepository extends JpaRepository<TrainingRegistration, UUID> {
  Optional<TrainingRegistration> findBySessionIdAndUserId(UUID sessionId, UUID userId);
  List<TrainingRegistration> findBySessionId(UUID sessionId);
  List<TrainingRegistration> findByUserId(UUID userId);

  @Query("SELECT COUNT(r) FROM TrainingRegistration r WHERE r.session.id = :sessionId AND r.status = 'registered'")
  long countActiveBySessionId(UUID sessionId);
}
