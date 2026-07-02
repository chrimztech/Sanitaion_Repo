package com.unza.ubirr.repository;

import com.unza.ubirr.model.ContentFile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentFileRepository extends JpaRepository<ContentFile, UUID> {
  List<ContentFile> findByContentItem_IdOrderByCreatedAtAsc(UUID contentItemId);
}
