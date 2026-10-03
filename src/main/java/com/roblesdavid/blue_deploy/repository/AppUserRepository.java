package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad AppUser.
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<AppUser> findByRole(UserRole role);

    List<AppUser> findByRoleAndActiveTrue(UserRole role);
}
