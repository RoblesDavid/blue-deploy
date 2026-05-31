package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.DashboardSummaryDTO;
import com.roblesdavid.blue_deploy.model.CheckStatus;
import com.roblesdavid.blue_deploy.model.DeploymentStatus;
import com.roblesdavid.blue_deploy.repository.DeploymentRepository;
import com.roblesdavid.blue_deploy.repository.SecurityCheckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Construye el resumen de alto nivel que consume el dashboard web.
 */
@Service
public class DashboardService {

    private static final String CROWDSTRIKE_CHECK = "CROWDSTRIKE_VALIDATED";
    private static final String MICROCLAUDIA_CHECK = "MICROCLAUDIA_VALIDATED";

    private final DeploymentRepository deploymentRepository;
    private final SecurityCheckRepository securityCheckRepository;

    public DashboardService(
            DeploymentRepository deploymentRepository,
            SecurityCheckRepository securityCheckRepository
    ) {
        this.deploymentRepository = deploymentRepository;
        this.securityCheckRepository = securityCheckRepository;
    }

    /**
     * Calcula métricas agregadas de deployments y checks pendientes.
     *
     * @return DTO con métricas globales del entorno
     */
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();

        dto.totalDeployments = deploymentRepository.count();
        dto.inProgress = deploymentRepository.countByDeploymentStatus(DeploymentStatus.IN_PROGRESS);
        dto.domainJoined = deploymentRepository.countByDeploymentStatus(DeploymentStatus.DOMAIN_JOINED);
        dto.postDomainPending = deploymentRepository.countByDeploymentStatus(DeploymentStatus.POST_DOMAIN_PENDING);
        dto.completed = deploymentRepository.countByDeploymentStatus(DeploymentStatus.COMPLETED);
        dto.completedWithWarnings = deploymentRepository.countByDeploymentStatus(DeploymentStatus.COMPLETED_WITH_WARNINGS);
        dto.failed = deploymentRepository.countByDeploymentStatus(DeploymentStatus.FAILED);
        dto.nonCompliant = deploymentRepository.countByDeploymentStatus(DeploymentStatus.NON_COMPLIANT);

        List<CheckStatus> pendingStatuses = List.of(CheckStatus.PENDING, CheckStatus.UNKNOWN);

        dto.pendingCrowdStrike = securityCheckRepository.countDistinctDeploymentsByCheckNameAndCheckStatusIn(
                CROWDSTRIKE_CHECK,
                pendingStatuses
        );

        dto.pendingMicroClaudia = securityCheckRepository.countDistinctDeploymentsByCheckNameAndCheckStatusIn(
                MICROCLAUDIA_CHECK,
                pendingStatuses
        );

        return dto;
    }
}
