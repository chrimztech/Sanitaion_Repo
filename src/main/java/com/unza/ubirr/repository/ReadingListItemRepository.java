package com.unza.ubirr.repository;

import com.unza.ubirr.model.ReadingListItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingListItemRepository extends JpaRepository<ReadingListItem, UUID> {
  List<ReadingListItem> findByReadingList_IdOrderByPositionAsc(UUID readingListId);
  void deleteByReadingList_Id(UUID readingListId);
}
