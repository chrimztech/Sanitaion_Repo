package com.unza.ubirr.repository;

import com.unza.ubirr.model.ApprovalWorkflow;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, UUID> {
  void deleteByContentItem_Id(UUID contentItemId);
}
