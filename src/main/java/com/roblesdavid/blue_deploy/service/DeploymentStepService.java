package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.DeploymentStepRequestDTO;
import com.roblesdavid.blue_deploy.dto.DeploymentStepResponseDTO;
import com.roblesdavid.blue_deploy.mapper.DeploymentStepMapper;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStatus;
import com.roblesdavid.blue_deploy.model.DeploymentStep;
import com.roblesdavid.blue_deploy.model.StepStatus;
import com.roblesdavid.blue_deploy.repository.DeploymentStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestiona el registro de pasos del deployment y actualiza su estado agregado.
 */
@Service
public class DeploymentStepService {

    private final DeploymentStepRepository repository;
    private final DeploymentService deploymentService;

    public DeploymentStepService(
            DeploymentStepRepository repository,
            DeploymentService deploymentService
    ) {
        this.repository = repository;
        this.deploymentService = deploymentService;
    }

    /**
     * Registra un paso ejecutado para el deployment indicado.
     *
     * @param deploymentId id funcional del deployment
     * @param dto información del paso reportado
     * @return paso persistido en formato DTO
     */
    @Transactional
    public DeploymentStepResponseDTO registerStep(String deploymentId, DeploymentStepRequestDTO dto) {
        Deployment deployment = deploymentService.getDeploymentEntityByDeploymentId(deploymentId);

        LocalDateTime now = LocalDateTime.now();

        DeploymentStep step = new DeploymentStep();
        step.setDeployment(deployment);
        step.setStepName(dto.getStepName());
        step.setStepStatus(dto.getStepStatus());
        step.setMessage(dto.getMessage());
        step.setErrorCode(dto.getErrorCode());
        step.setRawOutput(dto.getRawOutput());
        step.setStartedAt(dto.getStartedAt());
        step.setFinishedAt(dto.getFinishedAt());
        step.setReportedAt(now);

        updateDeploymentStatusFromStep(deployment, dto, now);

        DeploymentStep saved = repository.save(step);

        return DeploymentStepMapper.toDTO(saved);
    }

    /**
     * Lista los pasos de un deployment en orden cronológico de reporte.
     *
     * @param deploymentId id funcional del deployment
     * @return colección de pasos
     */
    @Transactional(readOnly = true)
    public List<DeploymentStepResponseDTO> getStepsByDeploymentId(String deploymentId) {
        Deployment deployment = deploymentService.getDeploymentEntityByDeploymentId(deploymentId);

        return repository.findByDeploymentOrderByReportedAtAsc(deployment)
                .stream()
                .map(DeploymentStepMapper::toDTO)
                .toList();
    }

    /**
     * Traduce el estado de un paso al estado global del deployment.
     */
    private void updateDeploymentStatusFromStep(
            Deployment deployment,
            DeploymentStepRequestDTO dto,
            LocalDateTime now
    ) {
        deployment.setCurrentStep(dto.getStepName());
        deployment.setLastSeenAt(now);

        if (dto.getStepStatus() == StepStatus.FAILED) {
            deployment.setDeploymentStatus(DeploymentStatus.FAILED);
            deployment.setFinishedAt(now);
        } else if ("DOMAIN_JOIN".equals(dto.getStepName()) && dto.getStepStatus() == StepStatus.SUCCESS) {
            deployment.setDeploymentStatus(DeploymentStatus.DOMAIN_JOINED);
        } else if ("GPUPDATE".equals(dto.getStepName()) && dto.getStepStatus() == StepStatus.SUCCESS) {
            deployment.setDeploymentStatus(DeploymentStatus.POST_DOMAIN_PENDING);
        } else if ("FINAL_SECURITY_VALIDATION".equals(dto.getStepName()) && dto.getStepStatus() == StepStatus.SUCCESS) {
            deployment.setDeploymentStatus(DeploymentStatus.COMPLETED);
            deployment.setFinishedAt(now);
        }

        deploymentService.saveDeployment(deployment);
    }
}
