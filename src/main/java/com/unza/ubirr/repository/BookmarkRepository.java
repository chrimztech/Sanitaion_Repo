package com.unza.ubirr.repository;

import com.unza.ubirr.model.Bookmark;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
  @Query("SELECT b FROM Bookmark b JOIN FETCH b.contentItem ci LEFT JOIN FETCH ci.category WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
  List<Bookmark> findByUser_IdOrderByCreatedAtDesc(@Param("userId") UUID userId);
  Optional<Bookmark> findByUser_IdAndContentItem_Id(UUID userId, UUID contentItemId);
  boolean existsByUser_IdAndContentItem_Id(UUID userId, UUID contentItemId);
  void deleteByContentItem_Id(UUID contentItemId);
}
