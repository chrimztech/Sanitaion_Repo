package com.unza.ubirr.repository;

import com.unza.ubirr.model.UserSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
  List<UserSession> findByUser_IdOrderByCreatedAtDesc(UUID userId);
  Optional<UserSession> findBySessionId(String sessionId);
  void deleteByUser_Id(UUID userId);

  @Query("""
    SELECT s FROM UserSession s LEFT JOIN FETCH s.user
    ORDER BY s.createdAt DESC
    LIMIT 300
    """)
  List<UserSession> findRecentWithUser();
}
