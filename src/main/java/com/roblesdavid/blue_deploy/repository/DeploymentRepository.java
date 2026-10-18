package com.roblesdavid.blue_deploy.repository;

import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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
     * Comprueba si existe un deployment por su identificador funcional único.
     *
     * @param deploymentId identificador funcional
     * @return true si existe, false en caso contrario
     */
    boolean existsByDeploymentId(String deploymentId);

    /**
     * Cuenta deployments por estado global.
     *
     * @param deploymentStatus estado de deployment
     * @return número de deployments en ese estado
     */
    long countByDeploymentStatus(DeploymentStatus deploymentStatus);

    /**
     * Busca deployments asignados a un técnico concreto por su entidad de usuario.
     *
     * @param technician entidad del técnico
     * @return lista de deployments asignados
     */
    List<Deployment> findByTechnician(AppUser technician);

    /**
     * Busca deployments que no tienen ningún técnico asignado (huérfanos de OSDCloud).
     *
     * @return lista de deployments sin asignar
     */
    List<Deployment> findByTechnicianIsNull();

    /**
     * Busca deployments asignados a un técnico por su ID de usuario.
     *
     * @param technicianId ID del técnico
     * @return lista de deployments asignados
     */
    List<Deployment> findByTechnicianId(Long technicianId);

    /**
     * Busca deployments asignados a un técnico por su nombre de usuario (username).
     *
     * @param username nombre de usuario del técnico
     * @return lista de deployments asignados
     */
    List<Deployment> findByTechnicianUsername(String username);

    long countByTechnicianUsername(String username);

    long countByTechnicianUsernameAndDeploymentStatus(String username, DeploymentStatus status);

    Optional<Deployment> findTopByDeploymentStatusOrderByLastSeenAtDesc(DeploymentStatus deploymentStatus);

    Optional<Deployment> findTopByTechnicianUsernameAndDeploymentStatusOrderByLastSeenAtDesc(String username, DeploymentStatus deploymentStatus);
}
