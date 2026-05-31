package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.DeploymentRequestDTO;
import com.roblesdavid.blue_deploy.dto.DeploymentResponseDTO;
import com.roblesdavid.blue_deploy.mapper.DeploymentMapper;
import com.roblesdavid.blue_deploy.model.ComplianceStatus;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStatus;
import com.roblesdavid.blue_deploy.repository.DeploymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orquesta el ciclo de vida de los deployments y su persistencia.
 */
@Service
public class DeploymentService {

    private final DeploymentRepository repository;

    public DeploymentService(DeploymentRepository repository) {
        this.repository = repository;
    }

    /**
     * Crea un deployment nuevo con estado inicial.
     *
     * @param dto datos de creación del deployment
     * @return deployment persistido en formato DTO
     */
    @Transactional
    public DeploymentResponseDTO createDeployment(DeploymentRequestDTO dto) {
        repository.findByDeploymentId(dto.getDeploymentId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un deployment con id: " + dto.getDeploymentId());
        });

        LocalDateTime now = LocalDateTime.now();

        Deployment deployment = new Deployment();
        deployment.setDeploymentId(dto.getDeploymentId());
        deployment.setHostname(dto.getHostname());
        deployment.setDeviceModel(dto.getDeviceModel());
        deployment.setDomainName(dto.getDomainName());
        deployment.setCurrentStep("DEPLOYMENT_CREATED");
        deployment.setDeploymentStatus(DeploymentStatus.IN_PROGRESS);
        deployment.setComplianceStatus(ComplianceStatus.PENDING);
        deployment.setStartedAt(now);
        deployment.setLastSeenAt(now);

        Deployment saved = repository.save(deployment);

        return DeploymentMapper.toDTO(saved);
    }

    /**
     * Obtiene todos los deployments y los adapta a DTO de salida.
     *
     * @return lista de deployments
     */
    @Transactional(readOnly = true)
    public List<DeploymentResponseDTO> getAllDeployments() {
        return repository.findAll()
                .stream()
                .map(DeploymentMapper::toDTO)
                .toList();
    }

    /**
     * Busca un deployment por su id funcional.
     *
     * @param deploymentId identificador funcional del deployment
     * @return deployment encontrado en formato DTO
     */
    @Transactional(readOnly = true)
    public DeploymentResponseDTO getDeploymentByDeploymentId(String deploymentId) {
        Deployment deployment = repository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment no encontrado: " + deploymentId));

        return DeploymentMapper.toDTO(deployment);
    }

    /**
     * Devuelve la entidad Deployment para reutilizarla desde otros servicios.
     *
     * @param deploymentId identificador funcional del deployment
     * @return entidad deployment gestionada por JPA
     */
    public Deployment getDeploymentEntityByDeploymentId(String deploymentId) {
        return repository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment no encontrado: " + deploymentId));
    }

    /**
     * Persiste cambios de estado sobre un deployment existente.
     *
     * @param deployment entidad a guardar
     * @return entidad guardada
     */
    @Transactional
    public Deployment saveDeployment(Deployment deployment) {
        return repository.save(deployment);
    }
}
