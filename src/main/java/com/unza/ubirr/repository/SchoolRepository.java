package com.unza.ubirr.repository;

import com.unza.ubirr.model.School;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, UUID> {
}
