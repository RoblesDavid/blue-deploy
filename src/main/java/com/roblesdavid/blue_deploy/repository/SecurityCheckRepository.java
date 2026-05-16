package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.CheckStatus;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.SecurityCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para checks de seguridad asociados a deployments.
 */
public interface SecurityCheckRepository extends JpaRepository<SecurityCheck, Long> {

    /**
     * Devuelve los checks de un deployment ordenados por fecha de reporte.
     *
     * @param deployment deployment padre
     * @return lista cronológica de checks
     */
    List<SecurityCheck> findByDeploymentOrderByReportedAtAsc(Deployment deployment);

}
