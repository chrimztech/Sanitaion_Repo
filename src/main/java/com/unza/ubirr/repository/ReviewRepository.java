package com.unza.ubirr.repository;

import com.unza.ubirr.model.Review;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

  @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.contentItem.id = :contentItemId ORDER BY r.createdAt DESC")
  List<Review> findByContentItem_IdOrderByCreatedAtDesc(@Param("contentItemId") UUID contentItemId);

  Optional<Review> findByContentItem_IdAndUser_Id(UUID contentItemId, UUID userId);

  @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.contentItem.id = :contentItemId")
  double averageRating(@Param("contentItemId") UUID contentItemId);

  long countByContentItem_Id(UUID contentItemId);

  interface RatingAggRow {
    UUID getContentItemId();
    Double getAvgRating();
    Long getReviewCount();
  }

  @Query("SELECT r.contentItem.id AS contentItemId, AVG(r.rating) AS avgRating, COUNT(r) AS reviewCount "
    + "FROM Review r WHERE r.contentItem.id IN :contentItemIds GROUP BY r.contentItem.id")
  List<RatingAggRow> ratingSummaries(@Param("contentItemIds") List<UUID> contentItemIds);

  @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.contentItem ORDER BY r.createdAt DESC")
  List<Review> findAllForModeration();

  void deleteByContentItem_Id(UUID contentItemId);
}
