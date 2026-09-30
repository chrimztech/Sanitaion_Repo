package com.unza.ubirr.repository;

import com.unza.ubirr.model.ViewRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ViewRecordRepository extends JpaRepository<ViewRecord, Long> {
  @Query("SELECT v FROM ViewRecord v JOIN FETCH v.contentItem ci LEFT JOIN FETCH ci.category WHERE v.user.id = :userId ORDER BY v.viewedAt DESC LIMIT 50")
  List<ViewRecord> findTop50ByUser_IdOrderByViewedAtDesc(@Param("userId") UUID userId);

  @Modifying
  @Query("UPDATE ViewRecord v SET v.user = null WHERE v.user.id = :userId")
  void nullifyUser(@Param("userId") UUID userId);

  @Query("SELECT COUNT(v) FROM ViewRecord v WHERE v.contentItem.id = :contentItemId")
  long countByContentItemId(@Param("contentItemId") UUID contentItemId);

  @Query("SELECT v.contentItem.id, COUNT(v) FROM ViewRecord v GROUP BY v.contentItem.id")
  List<Object[]> countGroupedByContentItem();

  @Query("SELECT v.contentItem.id, v.contentItem.title, v.user.id, v.user.fullName, v.user.email, COUNT(v), MAX(v.viewedAt) "
    + "FROM ViewRecord v GROUP BY v.contentItem.id, v.contentItem.title, v.user.id, v.user.fullName, v.user.email")
  List<Object[]> countGroupedByContentItemAndUser();

  void deleteByContentItem_Id(UUID contentItemId);
}
