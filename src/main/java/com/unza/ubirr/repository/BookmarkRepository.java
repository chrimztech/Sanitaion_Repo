package com.unza.ubirr.repository;

import com.unza.ubirr.model.Bookmark;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
  List<Bookmark> findByUser_IdOrderByCreatedAtDesc(UUID userId);
  Optional<Bookmark> findByUser_IdAndContentItem_Id(UUID userId, UUID contentItemId);
  boolean existsByUser_IdAndContentItem_Id(UUID userId, UUID contentItemId);
}
