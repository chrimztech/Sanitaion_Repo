package com.unza.ubirr.repository;

import com.unza.ubirr.model.HeroBanner;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeroBannerRepository extends JpaRepository<HeroBanner, UUID> {
  List<HeroBanner> findAllByActiveTrueOrderByDisplayOrderAsc();
  List<HeroBanner> findAllByOrderByDisplayOrderAsc();
}
