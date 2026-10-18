package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.DeploymentRequestDTO;
import com.roblesdavid.blue_deploy.dto.DeploymentResponseDTO;
import com.roblesdavid.blue_deploy.dto.UpdateDeploymentStatusRequestDTO;
import com.roblesdavid.blue_deploy.mapper.DeploymentMapper;
import com.roblesdavid.blue_deploy.mapper.DeploymentStepMapper;
import com.roblesdavid.blue_deploy.mapper.SecurityCheckMapper;
import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.AuditAction;
import com.roblesdavid.blue_deploy.model.ComplianceStatus;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStatus;
import com.roblesdavid.blue_deploy.model.DeploymentStep;
import com.roblesdavid.blue_deploy.model.SecurityCheck;
import com.roblesdavid.blue_deploy.repository.AppUserRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentStepRepository;
import com.roblesdavid.blue_deploy.repository.SecurityCheckRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AppUserRepository userRepository;
    private final DeploymentStepRepository stepRepository;
    private final SecurityCheckRepository checkRepository;
    private final AuditLogService auditLogService;

    public DeploymentService(
            DeploymentRepository repository,
            AppUserRepository userRepository,
            DeploymentStepRepository stepRepository,
            SecurityCheckRepository checkRepository,
            AuditLogService auditLogService
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.stepRepository = stepRepository;
        this.checkRepository = checkRepository;
        this.auditLogService = auditLogService;
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

        if (dto.getTechnicianId() != null) {
            AppUser technician = userRepository.findById(dto.getTechnicianId())
                    .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado con id: " + dto.getTechnicianId()));
            deployment.setTechnician(technician);
        }

        Deployment saved = repository.save(deployment);

        auditLogService.recordAudit(
                AuditAction.SCRIPT_EVENT_RECEIVED,
                getCurrentUsername(),
                saved.getDeploymentId(),
                "DEPLOYMENT",
                "Creación de nuevo despliegue para host '" + saved.getHostname() + "'",
                null
        );

        return DeploymentMapper.toDTO(saved);
    }

    /**
     * Reasigna un deployment a un técnico específico.
     *
     * @param deploymentId id funcional del deployment
     * @param technicianId id del técnico a asignar
     * @return deployment actualizado en formato DTO
     */
    @Transactional
    public DeploymentResponseDTO assignTechnician(String deploymentId, Long technicianId) {
        Deployment deployment = repository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment no encontrado: " + deploymentId));

        AppUser technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado con id: " + technicianId));

        deployment.setTechnician(technician);
        Deployment saved = repository.save(deployment);

        auditLogService.recordAudit(
                AuditAction.DEPLOYMENT_ASSIGNED,
                getCurrentUsername(),
                saved.getDeploymentId(),
                "DEPLOYMENT",
                "Asignación de despliegue a técnico @" + technician.getUsername() + " (" + technician.getName() + ")",
                null
        );

        return DeploymentMapper.toDTO(saved);
    }

    /**
     * Permite a un técnico autenticado reclamar o autoasignarse un deployment huérfano.
     *
     * @param deploymentId id funcional del deployment
     * @param username nombre de usuario del técnico
     * @return deployment actualizado
     */
    @Transactional
    public DeploymentResponseDTO claimDeployment(String deploymentId, String username) {
        Deployment deployment = repository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment no encontrado: " + deploymentId));

        AppUser technician = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con username: " + username));

        deployment.setTechnician(technician);
        Deployment saved = repository.save(deployment);

        auditLogService.recordAudit(
                AuditAction.DEPLOYMENT_ASSIGNED,
                username,
                saved.getDeploymentId(),
                "DEPLOYMENT",
                "Técnico @" + username + " (" + technician.getName() + ") se autoasignó el despliegue",
                null
        );

        return DeploymentMapper.toDTO(saved);
    }

    /**
     * Obtiene los deployments accesibles según el rol del usuario autenticado.
     * - ADMIN: visualiza todos los deployments.
     * - TECHNICIAN: visualiza únicamente sus deployments asignados.
     *
     * @param authentication objeto de autenticación actual
     * @return lista de deployments filtrados
     */
    @Transactional(readOnly = true)
    public List<DeploymentResponseDTO> getDeploymentsForUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isTechnician = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_TECHNICIAN".equals(a.getAuthority()));
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

            if (isTechnician && !isAdmin) {
                return getMyDeployments(authentication.getName());
            }
        }
        return getAllDeployments();
    }

    /**
     * Obtiene todos los deployments asignados al técnico identificado por su username.
     *
     * @param username nombre de usuario del técnico
     * @return lista de deployments del técnico
     */
    @Transactional(readOnly = true)
    public List<DeploymentResponseDTO> getMyDeployments(String username) {
        return repository.findByTechnicianUsername(username)
                .stream()
                .map(DeploymentMapper::toDTO)
                .toList();
    }

    /**
     * Obtiene los deployments que no tienen técnico asignado (disponibles para autoasignación).
     *
     * @return lista de deployments sin asignar
     */
    @Transactional(readOnly = true)
    public List<DeploymentResponseDTO> getUnassignedDeployments() {
        return repository.findByTechnicianIsNull()
                .stream()
                .map(DeploymentMapper::toDTO)
                .toList();
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
     * Busca un deployment por su id funcional, cargando su lista completa de pasos y comprobaciones de seguridad.
     *
     * @param deploymentId identificador funcional del deployment
     * @return deployment encontrado en formato DTO con steps y checks incluidos
     */
    @Transactional(readOnly = true)
    public DeploymentResponseDTO getDeploymentByDeploymentId(String deploymentId) {
        Deployment deployment = repository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment no encontrado: " + deploymentId));

        DeploymentResponseDTO dto = DeploymentMapper.toDTO(deployment);

        List<DeploymentStep> steps = stepRepository.findByDeploymentOrderByReportedAtAsc(deployment);
        List<SecurityCheck> checks = checkRepository.findByDeploymentOrderByReportedAtAsc(deployment);

        dto.steps = steps.stream().map(DeploymentStepMapper::toDTO).toList();
        dto.checks = checks.stream().map(SecurityCheckMapper::toDTO).toList();

        return dto;
    }

    /**
     * Actualiza manualmente el estado, compliance o fase de un despliegue, permitiendo a técnicos y administradores
     * corregir discrepancias operativas o forzar estados del ciclo de vida.
     *
     * @param deploymentId identificador del despliegue
     * @param dto datos de actualización manual
     * @param username usuario operador que ejecuta el cambio
     * @return despliegue actualizado
     */
    @Transactional
    public DeploymentResponseDTO updateDeploymentStatus(String deploymentId, UpdateDeploymentStatusRequestDTO dto, String username) {
        Deployment deployment = repository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment no encontrado: " + deploymentId));

        String oldStatus = deployment.getDeploymentStatus() != null ? deployment.getDeploymentStatus().name() : "NONE";
        String oldCompliance = deployment.getComplianceStatus() != null ? deployment.getComplianceStatus().name() : "NONE";

        LocalDateTime now = LocalDateTime.now();

        if (dto.getDeploymentStatus() != null && !dto.getDeploymentStatus().isBlank()) {
            DeploymentStatus newStatus = DeploymentStatus.valueOf(dto.getDeploymentStatus().toUpperCase());
            deployment.setDeploymentStatus(newStatus);
            if (newStatus == DeploymentStatus.COMPLETED || newStatus == DeploymentStatus.COMPLETED_WITH_WARNINGS || newStatus == DeploymentStatus.FAILED) {
                if (deployment.getFinishedAt() == null) {
                    deployment.setFinishedAt(now);
                }
            } else if (newStatus == DeploymentStatus.IN_PROGRESS) {
                deployment.setFinishedAt(null);
            }
        }

        if (dto.getComplianceStatus() != null && !dto.getComplianceStatus().isBlank()) {
            ComplianceStatus newCompliance = ComplianceStatus.valueOf(dto.getComplianceStatus().toUpperCase());
            deployment.setComplianceStatus(newCompliance);
        }

        if (dto.getCurrentStep() != null && !dto.getCurrentStep().isBlank()) {
            deployment.setCurrentStep(dto.getCurrentStep().trim());
        }

        deployment.setLastSeenAt(now);
        Deployment saved = repository.save(deployment);

        String noteDetail = (dto.getNote() != null && !dto.getNote().isBlank()) ? " | Nota: " + dto.getNote().trim() : "";
        auditLogService.recordAudit(
                AuditAction.DEPLOYMENT_STATUS_CHANGED,
                username != null ? username : getCurrentUsername(),
                saved.getDeploymentId(),
                "DEPLOYMENT",
                "Actualización manual de estado: [" + oldStatus + " -> " + saved.getDeploymentStatus() +
                        ", Compliance: " + oldCompliance + " -> " + saved.getComplianceStatus() +
                        ", Fase: " + saved.getCurrentStep() + "]" + noteDetail,
                null
        );

        return getDeploymentByDeploymentId(saved.getDeploymentId());
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

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
