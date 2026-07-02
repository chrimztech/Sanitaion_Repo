package com.unza.ubirr.repository;

import com.unza.ubirr.model.ReadingList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingListRepository extends JpaRepository<ReadingList, UUID> {
  List<ReadingList> findByUser_IdOrderByUpdatedAtDesc(UUID userId);
  List<ReadingList> findByPublicListTrueOrderByUpdatedAtDesc();
}
