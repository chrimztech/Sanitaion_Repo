package com.unza.ubirr.repository;

import com.unza.ubirr.model.ContentTag;
import com.unza.ubirr.model.ContentTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentTagRepository extends JpaRepository<ContentTag, ContentTagId> {
}
