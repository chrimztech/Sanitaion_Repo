package com.unza.ubirr.repository;

import com.unza.ubirr.model.ContentFile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentFileRepository extends JpaRepository<ContentFile, UUID> {
  List<ContentFile> findByContentItem_IdOrderByCreatedAtAsc(UUID contentItemId);

  List<ContentFile> findByContentItem_IdInOrderByCreatedAtAsc(List<UUID> contentItemIds);

  // Exact byte-for-byte duplicate of a file already attached to a *different* resource.
  @Query("SELECT f FROM ContentFile f JOIN FETCH f.contentItem WHERE f.checksum = :checksum AND f.contentItem.id <> :excludeContentId")
  Optional<ContentFile> findFirstByChecksumExcludingItem(@Param("checksum") String checksum, @Param("excludeContentId") UUID excludeContentId);

  // Same filename + size attached to a different resource — checked against that resource's
  // author/publication year in Java, since matching both of those plus filename+size is a
  // strong signal of an accidental re-upload of the same source material.
  @Query("SELECT f FROM ContentFile f JOIN FETCH f.contentItem WHERE f.originalFileName = :fileName AND f.fileSize = :fileSize AND f.contentItem.id <> :excludeContentId")
  List<ContentFile> findByFileNameAndSizeExcludingItem(@Param("fileName") String fileName, @Param("fileSize") Long fileSize, @Param("excludeContentId") UUID excludeContentId);
}
