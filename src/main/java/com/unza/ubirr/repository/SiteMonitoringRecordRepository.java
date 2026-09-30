package com.unza.ubirr.repository;

import com.unza.ubirr.model.SiteMonitoringRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SiteMonitoringRecordRepository extends JpaRepository<SiteMonitoringRecord, UUID> {
  // Fetch-joins site + recordedBy — this codebase's entities use plain public fields (no
  // getters), so reading r.site.id on a cold lazy proxy would silently come back null
  // instead of throwing (see ReadingListItemRepository for the full explanation).
  @Query("SELECT r FROM SiteMonitoringRecord r JOIN FETCH r.site LEFT JOIN FETCH r.recordedBy WHERE r.site.id = :siteId ORDER BY r.visitDate DESC")
  List<SiteMonitoringRecord> findBySiteIdOrderByVisitDateDesc(@Param("siteId") UUID siteId);

  @Query("SELECT r FROM SiteMonitoringRecord r JOIN FETCH r.site WHERE r.id = :id")
  Optional<SiteMonitoringRecord> findByIdWithSite(@Param("id") UUID id);

  long countBySite_Id(UUID siteId);
}
