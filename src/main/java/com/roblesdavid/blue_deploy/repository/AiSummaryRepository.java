package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.AiSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para resúmenes de incidencias generados por el motor de IA.
 */
public interface AiSummaryRepository extends JpaRepository<AiSummary, Long> {

    Optional<AiSummary> findTopByDeploymentIdOrderByCreatedAtDesc(String deploymentId);

    List<AiSummary> findByDeploymentIdOrderByCreatedAtDesc(String deploymentId);
}
