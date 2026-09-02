package com.unza.ubirr.repository;

import com.unza.ubirr.model.ContentItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentItemRepository extends JpaRepository<ContentItem, UUID>, JpaSpecificationExecutor<ContentItem> {
  Optional<ContentItem> findBySlug(String slug);

  @Modifying
  @Query("UPDATE ContentItem c SET c.author = null WHERE c.author.id = :userId")
  void nullifyAuthor(@Param("userId") UUID userId);

  @Modifying
  @Query("UPDATE ContentItem c SET c.viewCount = c.viewCount + 1 WHERE c.id = :id")
  int incrementViewCount(@Param("id") UUID id);

  @Modifying
  @Query("UPDATE ContentItem c SET c.thumbnailPath = :path WHERE c.id = :id AND c.thumbnailPath IS NULL")
  int setThumbnailIfAbsent(@Param("id") UUID id, @Param("path") String path);

  @Query("SELECT c FROM ContentItem c LEFT JOIN FETCH c.category LEFT JOIN FETCH c.department LEFT JOIN FETCH c.author ORDER BY c.updatedAt DESC")
  List<ContentItem> findAllForAdmin();

  @Query("SELECT c FROM ContentItem c LEFT JOIN FETCH c.category LEFT JOIN FETCH c.department LEFT JOIN FETCH c.author WHERE c.id = :id")
  Optional<ContentItem> findByIdForAdmin(@Param("id") UUID id);

  @Query(value = """
      SELECT * FROM content_items
      WHERE  status       = 'PUBLISHED'
        AND  access_level = 'PUBLIC'
        AND  deleted_at   IS NULL
        AND  search_vector @@ plainto_tsquery('english', :query)
        AND  (:categorySlug  IS NULL OR category_id   = (SELECT id FROM categories  WHERE slug = :categorySlug))
        AND  (:contentType   IS NULL OR content_type  = :contentType)
        AND  (:departmentId  IS NULL OR department_id = CAST(:departmentId AS uuid))
      ORDER BY ts_rank(search_vector, plainto_tsquery('english', :query)) DESC,
               published_at DESC
      LIMIT 60
      """, nativeQuery = true)
  List<ContentItem> fullTextSearch(
      @Param("query") String query,
      @Param("categorySlug") String categorySlug,
      @Param("contentType") String contentType,
      @Param("departmentId") String departmentId
  );
}
