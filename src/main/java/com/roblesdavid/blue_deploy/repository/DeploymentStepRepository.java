package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para pasos de deployment.
 */
public interface DeploymentStepRepository extends JpaRepository<DeploymentStep, Long> {

    /**
     * Devuelve los pasos de un deployment ordenados por su fecha de reporte.
     *
     * @param deployment deployment padre
     * @return lista ordenada de pasos
     */
    List<DeploymentStep> findByDeploymentOrderByReportedAtAsc(Deployment deployment);
}
