package com.roblesdavid.blue_deploy.mapper;

import com.roblesdavid.blue_deploy.dto.DeploymentStepResponseDTO;
import com.roblesdavid.blue_deploy.model.DeploymentStep;

/**
 * Mapper de entidad {@link DeploymentStep} a DTO de respuesta.
 */
public class DeploymentStepMapper {

    /**
     * Convierte un paso de deployment persistido a salida API.
     *
     * @param step paso a mapear
     * @return DTO de respuesta
     */
    public static DeploymentStepResponseDTO toDTO(DeploymentStep step) {
        DeploymentStepResponseDTO dto = new DeploymentStepResponseDTO();

        dto.id = step.getId();
        dto.deploymentId = step.getDeployment().getDeploymentId();
        dto.stepName = step.getStepName();
        dto.stepStatus = step.getStepStatus().name();
        dto.message = step.getMessage();
        dto.errorCode = step.getErrorCode();
        dto.rawOutput = step.getRawOutput();
        dto.startedAt = step.getStartedAt() != null ? step.getStartedAt().toString() : null;
        dto.finishedAt = step.getFinishedAt() != null ? step.getFinishedAt().toString() : null;
        dto.reportedAt = step.getReportedAt() != null ? step.getReportedAt().toString() : null;

        return dto;
    }
}
