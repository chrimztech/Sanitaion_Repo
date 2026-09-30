package com.unza.ubirr.repository;

import com.unza.ubirr.model.ReadingListItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadingListItemRepository extends JpaRepository<ReadingListItem, UUID> {
  // Fetch-joins contentItem — every entity in this codebase uses plain public fields (no
  // getters), and Hibernate's lazy-loading proxies only intercept method calls. Reading a
  // field like item.contentItem.title on a proxy that hasn't already been loaded elsewhere
  // in this request silently returns null instead of throwing, so this association must
  // always come back already-initialized.
  @Query("SELECT i FROM ReadingListItem i JOIN FETCH i.contentItem WHERE i.readingList.id = :readingListId ORDER BY i.position ASC")
  List<ReadingListItem> findByReadingList_IdOrderByPositionAsc(@Param("readingListId") UUID readingListId);

  @Query("SELECT i FROM ReadingListItem i JOIN FETCH i.readingList rl JOIN FETCH rl.user WHERE i.id = :itemId")
  Optional<ReadingListItem> findByIdWithOwner(@Param("itemId") UUID itemId);

  void deleteByReadingList_Id(UUID readingListId);
}
