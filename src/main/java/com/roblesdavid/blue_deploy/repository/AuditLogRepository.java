package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.AuditAction;
import com.roblesdavid.blue_deploy.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para registros de auditoría.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    List<AuditLog> findByTargetIdOrderByCreatedAtDesc(String targetId);

    List<AuditLog> findByActorUsernameOrderByCreatedAtDesc(String actorUsername);

    List<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action);
}
