package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.DashboardSummaryDTO;
import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.CheckStatus;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStatus;
import com.roblesdavid.blue_deploy.model.UserRole;
import com.roblesdavid.blue_deploy.repository.AppUserRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentRepository;
import com.roblesdavid.blue_deploy.repository.SecurityCheckRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Construye el resumen de alto nivel que consume el dashboard web.
 * Soporta métricas globales y métricas acotadas por rol de usuario.
 */
@Service
public class DashboardService {

    private static final String CROWDSTRIKE_CHECK = "CROWDSTRIKE_VALIDATED";
    private static final String MICROCLAUDIA_CHECK = "MICROCLAUDIA_VALIDATED";

    private final DeploymentRepository deploymentRepository;
    private final SecurityCheckRepository securityCheckRepository;
    private final AppUserRepository userRepository;

    public DashboardService(
            DeploymentRepository deploymentRepository,
            SecurityCheckRepository securityCheckRepository,
            AppUserRepository userRepository
    ) {
        this.deploymentRepository = deploymentRepository;
        this.securityCheckRepository = securityCheckRepository;
        this.userRepository = userRepository;
    }

    /**
     * Calcula métricas agregadas globales por defecto.
     *
     * @return DTO con métricas globales del entorno
     */
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        return getSummaryForUser(null);
    }

    /**
     * Calcula métricas según el contexto del usuario autenticado (global para ADMIN, acotado para TECHNICIAN).
     */
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummaryForUser(Authentication authentication) {
        String technicianUsername = null;
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isTechnician = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_TECHNICIAN".equals(a.getAuthority()));
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

            if (isTechnician && !isAdmin) {
                technicianUsername = authentication.getName();
            }
        }

        if (technicianUsername != null) {
            return getTechnicianSummary(technicianUsername);
        }
        return getGlobalSummary();
    }

    private DashboardSummaryDTO getGlobalSummary() {
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

        List<Deployment> allDeployments = deploymentRepository.findAll();
        List<AppUser> allTechs = userRepository.findByRole(UserRole.TECHNICIAN);
        dto.assignedTechnicians = (long) allTechs.size();

        Set<String> activeTechs = allDeployments.stream()
                .filter(d -> d.getTechnician() != null && List.of(DeploymentStatus.IN_PROGRESS, DeploymentStatus.DOMAIN_JOINED, DeploymentStatus.POST_DOMAIN_PENDING).contains(d.getDeploymentStatus()))
                .map(d -> d.getTechnician().getUsername())
                .collect(Collectors.toSet());
        dto.activeTechnicians = (long) activeTechs.size();

        List<Deployment> completedDeployments = allDeployments.stream()
                .filter(d -> d.getStartedAt() != null && d.getFinishedAt() != null && (d.getDeploymentStatus() == DeploymentStatus.COMPLETED || d.getDeploymentStatus() == DeploymentStatus.COMPLETED_WITH_WARNINGS))
                .toList();

        if (!completedDeployments.isEmpty()) {
            double avgMinutes = completedDeployments.stream()
                    .mapToDouble(d -> Duration.between(d.getStartedAt(), d.getFinishedAt()).toSeconds() / 60.0)
                    .average()
                    .orElse(0.0);
            dto.averageDeploymentTimeMinutes = Math.round(avgMinutes * 10.0) / 10.0;
        } else {
            dto.averageDeploymentTimeMinutes = 0.0;
        }

        deploymentRepository.findTopByDeploymentStatusOrderByLastSeenAtDesc(DeploymentStatus.FAILED)
                .ifPresent(d -> dto.lastFailedDeployment = d.getHostname() != null ? d.getHostname() : d.getDeploymentId());

        securityCheckRepository.findTopByCheckStatusInOrderByReportedAtDesc(List.of(CheckStatus.FAILED, CheckStatus.WARNING))
                .ifPresent(c -> dto.lastSecurityWarning = c.getCheckName() + " (" + (c.getMessage() != null ? c.getMessage() : c.getCheckStatus().name()) + ")");

        return dto;
    }

    private DashboardSummaryDTO getTechnicianSummary(String username) {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();

        dto.totalDeployments = deploymentRepository.countByTechnicianUsername(username);
        dto.inProgress = deploymentRepository.countByTechnicianUsernameAndDeploymentStatus(username, DeploymentStatus.IN_PROGRESS);
        dto.domainJoined = deploymentRepository.countByTechnicianUsernameAndDeploymentStatus(username, DeploymentStatus.DOMAIN_JOINED);
        dto.postDomainPending = deploymentRepository.countByTechnicianUsernameAndDeploymentStatus(username, DeploymentStatus.POST_DOMAIN_PENDING);
        dto.completed = deploymentRepository.countByTechnicianUsernameAndDeploymentStatus(username, DeploymentStatus.COMPLETED);
        dto.completedWithWarnings = deploymentRepository.countByTechnicianUsernameAndDeploymentStatus(username, DeploymentStatus.COMPLETED_WITH_WARNINGS);
        dto.failed = deploymentRepository.countByTechnicianUsernameAndDeploymentStatus(username, DeploymentStatus.FAILED);
        dto.nonCompliant = deploymentRepository.countByTechnicianUsernameAndDeploymentStatus(username, DeploymentStatus.NON_COMPLIANT);

        List<CheckStatus> pendingStatuses = List.of(CheckStatus.PENDING, CheckStatus.UNKNOWN);

        dto.pendingCrowdStrike = securityCheckRepository.countDistinctDeploymentsByCheckNameAndCheckStatusInAndTechnician(
                CROWDSTRIKE_CHECK,
                pendingStatuses,
                username
        );

        dto.pendingMicroClaudia = securityCheckRepository.countDistinctDeploymentsByCheckNameAndCheckStatusInAndTechnician(
                MICROCLAUDIA_CHECK,
                pendingStatuses,
                username
        );

        dto.assignedTechnicians = 1L;
        long activeCount = dto.inProgress + dto.domainJoined + dto.postDomainPending;
        dto.activeTechnicians = activeCount > 0 ? 1L : 0L;

        List<Deployment> techDeployments = deploymentRepository.findByTechnicianUsername(username);
        List<Deployment> completedDeployments = techDeployments.stream()
                .filter(d -> d.getStartedAt() != null && d.getFinishedAt() != null && (d.getDeploymentStatus() == DeploymentStatus.COMPLETED || d.getDeploymentStatus() == DeploymentStatus.COMPLETED_WITH_WARNINGS))
                .toList();

        if (!completedDeployments.isEmpty()) {
            double avgMinutes = completedDeployments.stream()
                    .mapToDouble(d -> Duration.between(d.getStartedAt(), d.getFinishedAt()).toSeconds() / 60.0)
                    .average()
                    .orElse(0.0);
            dto.averageDeploymentTimeMinutes = Math.round(avgMinutes * 10.0) / 10.0;
        } else {
            dto.averageDeploymentTimeMinutes = 0.0;
        }

        deploymentRepository.findTopByTechnicianUsernameAndDeploymentStatusOrderByLastSeenAtDesc(username, DeploymentStatus.FAILED)
                .ifPresent(d -> dto.lastFailedDeployment = d.getHostname() != null ? d.getHostname() : d.getDeploymentId());

        securityCheckRepository.findTopByDeploymentTechnicianUsernameAndCheckStatusInOrderByReportedAtDesc(username, List.of(CheckStatus.FAILED, CheckStatus.WARNING))
                .ifPresent(c -> dto.lastSecurityWarning = c.getCheckName() + " (" + (c.getMessage() != null ? c.getMessage() : c.getCheckStatus().name()) + ")");

        return dto;
    }
}
