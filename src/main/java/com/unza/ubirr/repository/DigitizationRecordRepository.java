package com.unza.ubirr.repository;

import com.unza.ubirr.model.DigitizationRecord;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DigitizationRecordRepository extends JpaRepository<DigitizationRecord, UUID> {
  @Modifying
  @Query("UPDATE DigitizationRecord d SET d.contentItem = null WHERE d.contentItem.id = :contentItemId")
  void unlinkContentItem(@Param("contentItemId") UUID contentItemId);
}
