package com.unza.ubirr.repository;

import com.unza.ubirr.model.EventRegistration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {
  Optional<EventRegistration> findByEventIdAndUserId(UUID eventId, UUID userId);

  // JOIN FETCH avoids the lazy-proxy blank-field pitfall — see TrainingRegistrationRepository.
  @Query("SELECT r FROM EventRegistration r JOIN FETCH r.user WHERE r.event.id = :eventId ORDER BY r.registeredAt DESC")
  List<EventRegistration> findByEventId(UUID eventId);

  @Query("SELECT r FROM EventRegistration r JOIN FETCH r.event WHERE r.user.id = :userId ORDER BY r.registeredAt DESC")
  List<EventRegistration> findByUserId(UUID userId);

  @Query("SELECT r FROM EventRegistration r JOIN FETCH r.user JOIN FETCH r.event WHERE r.id = :id")
  Optional<EventRegistration> findByIdWithDetails(UUID id);

  @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId AND r.status IN ('pending', 'approved')")
  long countActiveByEventId(UUID eventId);

  @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId AND r.status = 'pending'")
  long countPendingByEventId(UUID eventId);
}
