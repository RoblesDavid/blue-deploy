package com.roblesdavid.blue_deploy.mapper;

import com.roblesdavid.blue_deploy.dto.SecurityCheckResponseDTO;
import com.roblesdavid.blue_deploy.model.SecurityCheck;

/**
 * Mapper de entidad {@link SecurityCheck} a DTO expuesto por la API.
 */
public class SecurityCheckMapper {

    /**
     * Convierte un check persistido a DTO de respuesta.
     *
     * @param check check de seguridad
     * @return DTO de respuesta
     */
    public static SecurityCheckResponseDTO toDTO(SecurityCheck check) {
        SecurityCheckResponseDTO dto = new SecurityCheckResponseDTO();

        dto.id = check.getId();
        dto.deploymentId = check.getDeployment().getDeploymentId();
        dto.checkName = check.getCheckName();
        dto.checkStatus = check.getCheckStatus().name();
        dto.severity = check.getSeverity().name();
        dto.expectedValue = check.getExpectedValue();
        dto.actualValue = check.getActualValue();
        dto.message = check.getMessage();
        dto.checkedAt = check.getCheckedAt() != null ? check.getCheckedAt().toString() : null;
        dto.reportedAt = check.getReportedAt() != null ? check.getReportedAt().toString() : null;

        return dto;
    }
}
