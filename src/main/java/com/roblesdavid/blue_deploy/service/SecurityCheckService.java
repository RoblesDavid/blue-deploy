package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.SecurityCheckRequestDTO;
import com.roblesdavid.blue_deploy.dto.SecurityCheckResponseDTO;
import com.roblesdavid.blue_deploy.mapper.SecurityCheckMapper;
import com.roblesdavid.blue_deploy.model.CheckStatus;
import com.roblesdavid.blue_deploy.model.ComplianceStatus;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStatus;
import com.roblesdavid.blue_deploy.model.SecurityCheck;
import com.roblesdavid.blue_deploy.repository.SecurityCheckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityCheckService {

    private final SecurityCheckRepository repository;
    private final DeploymentService deploymentService;

    public SecurityCheckService(
            SecurityCheckRepository repository,
            DeploymentService deploymentService
    ) {
        this.repository = repository;
        this.deploymentService = deploymentService;
    }

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

    @Transactional(readOnly = true)
    public List<SecurityCheckResponseDTO> getChecksByDeploymentId(String deploymentId) {
        Deployment deployment = deploymentService.getDeploymentEntityByDeploymentId(deploymentId);

        List<SecurityCheck> checks = repository.findByDeploymentOrderByReportedAtAsc(deployment);

        return checks.stream()
                .map(SecurityCheckMapper::toDTO)
                .toList();
    }

    private void updateDeploymentCompliance(Deployment deployment, LocalDateTime now) {
        List<SecurityCheck> checks = repository.findByDeploymentOrderByReportedAtAsc(deployment);

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