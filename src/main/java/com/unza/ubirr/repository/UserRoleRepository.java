package com.unza.ubirr.repository;

import com.unza.ubirr.model.AppRole;
import com.unza.ubirr.model.UserRoleAssignment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRoleAssignment, UUID> {
  List<UserRoleAssignment> findByUser_Id(UUID userId);
  Optional<UserRoleAssignment> findByUser_IdAndRole(UUID userId, AppRole role);
  void deleteByUser_IdAndRole(UUID userId, AppRole role);
  long countByRole(AppRole role);
}
