package com.unza.ubirr.repository;

import com.unza.ubirr.model.EventEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity, UUID> {
  Optional<EventEntity> findBySlug(String slug);
}
