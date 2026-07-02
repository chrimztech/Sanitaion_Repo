package com.unza.ubirr.repository;

import com.unza.ubirr.model.AuditLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

  @Modifying
  @Query("UPDATE AuditLog a SET a.user = null WHERE a.user.id = :userId")
  void nullifyUser(@Param("userId") UUID userId);

  @Query("""
    SELECT a FROM AuditLog a LEFT JOIN FETCH a.user
    WHERE (:action IS NULL OR a.action LIKE :action)
    AND (:entityType IS NULL OR a.entityType = :entityType)
    ORDER BY a.createdAt DESC
    LIMIT 500
    """)
  List<AuditLog> findFiltered(
    @Param("action") String action,
    @Param("entityType") String entityType
  );
}
