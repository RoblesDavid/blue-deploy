package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Deployment}.
 */
public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    /**
     * Busca un deployment por su identificador funcional único.
     *
     * @param deploymentId identificador funcional
     * @return deployment encontrado, si existe
     */
    Optional<Deployment> findByDeploymentId(String deploymentId);

    /**
     * Cuenta deployments por estado global.
     *
     * @param deploymentStatus estado de deployment
     * @return número de deployments en ese estado
     */
    long countByDeploymentStatus(DeploymentStatus deploymentStatus);
}
