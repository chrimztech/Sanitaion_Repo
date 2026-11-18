package com.unza.ubirr.repository;

import com.unza.ubirr.model.TrainingRegistration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrainingRegistrationRepository extends JpaRepository<TrainingRegistration, UUID> {
  Optional<TrainingRegistration> findBySessionIdAndUserId(UUID sessionId, UUID userId);

  // JOIN FETCH is required: AppUser/TrainingSession use plain public fields rather than
  // getters, so Hibernate's lazy proxies can't intercept field reads — an uninitialized
  // r.user or r.session read outside this fetch silently returns blank defaults (e.g.
  // fullName="") instead of throwing, which is how the admin registrant list and the
  // user's "my registrations" view ended up showing blank names/emails/titles.
  @Query("SELECT r FROM TrainingRegistration r JOIN FETCH r.user WHERE r.session.id = :sessionId ORDER BY r.registeredAt DESC")
  List<TrainingRegistration> findBySessionId(UUID sessionId);

  @Query("SELECT r FROM TrainingRegistration r JOIN FETCH r.session WHERE r.user.id = :userId ORDER BY r.registeredAt DESC")
  List<TrainingRegistration> findByUserId(UUID userId);

  @Query("SELECT r FROM TrainingRegistration r JOIN FETCH r.user JOIN FETCH r.session WHERE r.id = :id")
  Optional<TrainingRegistration> findByIdWithDetails(UUID id);

  // A pending request still holds a seat until an admin decides, so capacity checks
  // and the "active" count treat pending + approved as occupying the roster.
  @Query("SELECT COUNT(r) FROM TrainingRegistration r WHERE r.session.id = :sessionId AND r.status IN ('pending', 'approved')")
  long countActiveBySessionId(UUID sessionId);

  @Query("SELECT COUNT(r) FROM TrainingRegistration r WHERE r.session.id = :sessionId AND r.status = 'pending'")
  long countPendingBySessionId(UUID sessionId);
}
