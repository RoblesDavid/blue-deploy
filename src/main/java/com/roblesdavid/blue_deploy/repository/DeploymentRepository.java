package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.Deployment;
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
}
