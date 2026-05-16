package com.roblesdavid.blue_deploy.mapper;

import com.roblesdavid.blue_deploy.dto.DeploymentResponseDTO;
import com.roblesdavid.blue_deploy.model.Deployment;

/**
 * Mapper de entidad {@link Deployment} a su DTO de salida.
 */
public class DeploymentMapper {

    /**
     * Adapta la entidad de dominio a formato API.
     *
     * @param deployment entidad a mapear
     * @return DTO de respuesta
     */
    public static DeploymentResponseDTO toDTO(Deployment deployment) {
        DeploymentResponseDTO dto = new DeploymentResponseDTO();

        dto.id = deployment.getId();
        dto.deploymentId = deployment.getDeploymentId();
        dto.hostname = deployment.getHostname();
        dto.deviceModel = deployment.getDeviceModel();
        dto.domainName = deployment.getDomainName();
        dto.currentStep = deployment.getCurrentStep();
        dto.deploymentStatus = deployment.getDeploymentStatus().name();
        dto.complianceStatus = deployment.getComplianceStatus().name();
        dto.startedAt = deployment.getStartedAt() != null ? deployment.getStartedAt().toString() : null;
        dto.finishedAt = deployment.getFinishedAt() != null ? deployment.getFinishedAt().toString() : null;
        dto.lastSeenAt = deployment.getLastSeenAt() != null ? deployment.getLastSeenAt().toString() : null;

        return dto;
    }
}
