package com.unza.ubirr.repository;

import com.unza.ubirr.model.DemonstrationSite;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemonstrationSiteRepository extends JpaRepository<DemonstrationSite, UUID> {
  Optional<DemonstrationSite> findBySlug(String slug);
  boolean existsBySlug(String slug);
  long countByOperationalStatus(String status);
}
