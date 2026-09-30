package com.unza.ubirr.repository;

import com.unza.ubirr.model.DownloadRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DownloadRecordRepository extends JpaRepository<DownloadRecord, Long> {

  @Modifying
  @Query("UPDATE DownloadRecord d SET d.user = null WHERE d.user.id = :userId")
  void nullifyUser(@Param("userId") UUID userId);
  @Query("SELECT COUNT(d) FROM DownloadRecord d WHERE d.contentFile.contentItem.id = :contentItemId")
  long countByContentItemId(@Param("contentItemId") UUID contentItemId);

  @Query("SELECT d.contentFile.contentItem.id, COUNT(d) FROM DownloadRecord d GROUP BY d.contentFile.contentItem.id")
  List<Object[]> countGroupedByContentItem();

  @Query("SELECT d.contentFile.contentItem.id, d.contentFile.contentItem.title, d.user.id, d.user.fullName, d.user.email, COUNT(d), MAX(d.downloadedAt) "
    + "FROM DownloadRecord d GROUP BY d.contentFile.contentItem.id, d.contentFile.contentItem.title, d.user.id, d.user.fullName, d.user.email")
  List<Object[]> countGroupedByContentItemAndUser();

  // A derived (find-then-remove) delete here would navigate contentFile.contentItem.id,
  // which makes Hibernate materialize an uninitialized ContentFile proxy into the persistence
  // context. Direct field access on that cold proxy (this codebase uses public fields, not
  // getters) then silently reads as null instead of triggering initialization — poisoning the
  // very next ContentFile query in the same transaction (AdminService.deleteContent's file
  // cleanup loop). A bulk @Modifying delete goes straight to SQL and never creates proxies.
  @Modifying
  @Query("DELETE FROM DownloadRecord d WHERE d.contentFile.id IN (SELECT cf.id FROM ContentFile cf WHERE cf.contentItem.id = :contentItemId)")
  void deleteByContentFile_ContentItem_Id(@Param("contentItemId") UUID contentItemId);
}
