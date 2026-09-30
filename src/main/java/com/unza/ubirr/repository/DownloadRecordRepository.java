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

  void deleteByContentFile_ContentItem_Id(UUID contentItemId);
}
