package com.unza.ubirr.repository;

import com.unza.ubirr.model.ContentTypeDef;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentTypeDefRepository extends JpaRepository<ContentTypeDef, UUID> {
  List<ContentTypeDef> findAllByOrderByNameAsc();
  boolean existsBySlug(String slug);
}
