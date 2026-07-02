package com.unza.ubirr.repository;

import com.unza.ubirr.model.DigitizationRecord;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigitizationRecordRepository extends JpaRepository<DigitizationRecord, UUID> {
}
