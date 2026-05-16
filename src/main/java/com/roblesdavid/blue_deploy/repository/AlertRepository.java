package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad {@link Alert}.
 */
public interface AlertRepository extends JpaRepository<Alert, Long> {
}
