package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.SecurityCheckRequestDTO;
import com.roblesdavid.blue_deploy.dto.SecurityCheckResponseDTO;
import com.roblesdavid.blue_deploy.mapper.SecurityCheckMapper;
import com.roblesdavid.blue_deploy.model.AuditAction;
import com.roblesdavid.blue_deploy.model.CheckStatus;
import com.roblesdavid.blue_deploy.model.ComplianceStatus;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStatus;
import com.roblesdavid.blue_deploy.model.SecurityCheck;
import com.roblesdavid.blue_deploy.repository.SecurityCheckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona los checks de seguridad y la actualización del estado de cumplimiento.
 */
@Service
public class SecurityCheckService {

    private final SecurityCheckRepository repository;
    private final DeploymentService deploymentService;
    private final AuditLogService auditLogService;

    public SecurityCheckService(
            SecurityCheckRepository repository,
            DeploymentService deploymentService,
            AuditLogService auditLogService
    ) {
        this.repository = repository;
        this.deploymentService = deploymentService;
        this.auditLogService = auditLogService;
    }

    /**
     * Registra un check de seguridad para un deployment y recalcula compliance.
     *
     * @param deploymentId id funcional del deployment
     * @param dto resultado del check reportado
     * @return check persistido en formato DTO
     */
    @Transactional
    public SecurityCheckResponseDTO registerCheck(String deploymentId, SecurityCheckRequestDTO dto) {
        Deployment deployment = deploymentService.getDeploymentEntityByDeploymentId(deploymentId);

        LocalDateTime now = LocalDateTime.now();

        SecurityCheck check = new SecurityCheck();
        check.setDeployment(deployment);
        check.setCheckName(dto.getCheckName());
        check.setCheckStatus(dto.getCheckStatus());
        check.setSeverity(dto.getSeverity());
        check.setExpectedValue(dto.getExpectedValue());
        check.setActualValue(dto.getActualValue());
        check.setMessage(dto.getMessage());
        check.setCheckedAt(dto.getCheckedAt());
        check.setReportedAt(now);

        SecurityCheck saved = repository.save(check);

        updateDeploymentCompliance(deployment, now);

        return SecurityCheckMapper.toDTO(saved);
    }

    /**
     * Resuelve o remedia un check de seguridad existente (ej: corrección manual por un técnico)
     * y recalcula automáticamente el cumplimiento del despliegue.
     *
     * @param deploymentId id funcional del deployment
     * @param checkId id de la entidad del check
     * @param resolutionMessage mensaje o nota de remediación
     * @param actorUsername usuario que realiza la remediación
     * @return check actualizado
     */
    @Transactional
    public SecurityCheckResponseDTO resolveCheck(String deploymentId, Long checkId, String resolutionMessage, String actorUsername) {
        Deployment deployment = deploymentService.getDeploymentEntityByDeploymentId(deploymentId);

        SecurityCheck check = repository.findById(checkId)
                .orElseThrow(() -> new IllegalArgumentException("Check no encontrado con ID: " + checkId));

        if (!check.getDeployment().getId().equals(deployment.getId())) {
            throw new IllegalArgumentException("El check no pertenece al deployment indicado");
        }

        LocalDateTime now = LocalDateTime.now();
        check.setCheckStatus(CheckStatus.PASSED);
        check.setActualValue("Remediado (" + (actorUsername != null ? actorUsername : "Técnico") + ")");
        if (resolutionMessage != null && !resolutionMessage.isBlank()) {
            check.setMessage(resolutionMessage);
        } else {
            check.setMessage("Acción pendiente corregida y validada manualmente");
        }
        check.setCheckedAt(now);
        check.setReportedAt(now);

        SecurityCheck saved = repository.save(check);

        updateDeploymentCompliance(deployment, now);

        auditLogService.recordAudit(
                AuditAction.COMPLIANCE_STATUS_CHANGED,
                actorUsername != null ? actorUsername : "SYSTEM",
                deployment.getDeploymentId(),
                "SECURITY_CHECK",
                "Check '" + check.getCheckName() + "' resuelto/remediado manualmente: " + check.getMessage(),
                null
        );

        return SecurityCheckMapper.toDTO(saved);
    }

    /**
     * Lista todos los checks asociados a un deployment.
     *
     * @param deploymentId id funcional del deployment
     * @return checks ordenados por fecha de reporte
     */
    @Transactional(readOnly = true)
    public List<SecurityCheckResponseDTO> getChecksByDeploymentId(String deploymentId) {
        Deployment deployment = deploymentService.getDeploymentEntityByDeploymentId(deploymentId);

        List<SecurityCheck> checks = repository.findByDeploymentOrderByReportedAtAsc(deployment);

        return checks.stream()
                .map(SecurityCheckMapper::toDTO)
                .toList();
    }

    /**
     * Calcula el estado de cumplimiento final a partir de los checks acumulados.
     */
    private void updateDeploymentCompliance(Deployment deployment, LocalDateTime now) {
        List<SecurityCheck> allChecks = repository.findByDeploymentOrderByReportedAtAsc(deployment);

        // Agrupar por nombre de check y tomar el estado más reciente reportado
        Map<String, SecurityCheck> latestChecksByName = new LinkedHashMap<>();
        for (SecurityCheck c : allChecks) {
            latestChecksByName.put(c.getCheckName(), c);
        }

        Collection<SecurityCheck> checks = latestChecksByName.values();

        boolean hasFailed = checks.stream()
                .anyMatch(check -> check.getCheckStatus() == CheckStatus.FAILED);

        boolean hasPendingOrUnknown = checks.stream()
                .anyMatch(check ->
                        check.getCheckStatus() == CheckStatus.PENDING ||
                                check.getCheckStatus() == CheckStatus.UNKNOWN
                );

        boolean hasWarning = checks.stream()
                .anyMatch(check -> check.getCheckStatus() == CheckStatus.WARNING);

        boolean deploymentAlreadyFailed = deployment.getDeploymentStatus() == DeploymentStatus.FAILED;

        deployment.setLastSeenAt(now);

        if (hasFailed) {
            deployment.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);

            if (!deploymentAlreadyFailed) {
                deployment.setDeploymentStatus(DeploymentStatus.NON_COMPLIANT);
            }

        } else if (hasPendingOrUnknown) {
            deployment.setComplianceStatus(ComplianceStatus.PENDING);

            if (!deploymentAlreadyFailed) {
                deployment.setDeploymentStatus(DeploymentStatus.POST_DOMAIN_PENDING);
            }

        } else if (hasWarning) {
            deployment.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);

            if (!deploymentAlreadyFailed) {
                deployment.setDeploymentStatus(DeploymentStatus.COMPLETED_WITH_WARNINGS);
                deployment.setFinishedAt(now);
            }

        } else if (!checks.isEmpty()) {
            deployment.setComplianceStatus(ComplianceStatus.COMPLIANT);

            if (!deploymentAlreadyFailed) {
                deployment.setDeploymentStatus(DeploymentStatus.COMPLETED);
                deployment.setFinishedAt(now);
            }
        }

        deploymentService.saveDeployment(deployment);
    }
}
