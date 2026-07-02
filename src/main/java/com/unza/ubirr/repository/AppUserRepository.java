package com.unza.ubirr.repository;

import com.unza.ubirr.model.AppUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
  Optional<AppUser> findByEmailIgnoreCase(String email);
  boolean existsByEmailIgnoreCase(String email);
  Optional<AppUser> findByResetToken(String resetToken);
  Optional<AppUser> findByEmailVerificationToken(String emailVerificationToken);
  Optional<AppUser> findByUnzaId(String unzaId);
  boolean existsByUnzaId(String unzaId);

  @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.roles WHERE u.id = :id")
  Optional<AppUser> findByIdWithRoles(@Param("id") UUID id);

  @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.roles ORDER BY u.createdAt DESC")
  List<AppUser> findAllWithRoles();
}
