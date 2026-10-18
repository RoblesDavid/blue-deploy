package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.AiSummaryDTO;
import com.roblesdavid.blue_deploy.dto.DeploymentRequestDTO;
import com.roblesdavid.blue_deploy.dto.DeploymentResponseDTO;
import com.roblesdavid.blue_deploy.dto.UpdateDeploymentStatusRequestDTO;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStep;
import com.roblesdavid.blue_deploy.model.SecurityCheck;
import com.roblesdavid.blue_deploy.repository.DeploymentStepRepository;
import com.roblesdavid.blue_deploy.repository.SecurityCheckRepository;
import com.roblesdavid.blue_deploy.service.AiSummaryService;
import com.roblesdavid.blue_deploy.service.DeploymentReportService;
import com.roblesdavid.blue_deploy.service.DeploymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones de consulta, alta y gestión de deployments.
 */
@RestController
@RequestMapping("/api/deployments")
public class DeploymentController {

    private final DeploymentService service;
    private final DeploymentReportService reportService;
    private final DeploymentStepRepository stepRepository;
    private final SecurityCheckRepository checkRepository;
    private final AiSummaryService aiSummaryService;

    public DeploymentController(
            DeploymentService service,
            DeploymentReportService reportService,
            DeploymentStepRepository stepRepository,
            SecurityCheckRepository checkRepository,
            AiSummaryService aiSummaryService
    ) {
        this.service = service;
        this.reportService = reportService;
        this.stepRepository = stepRepository;
        this.checkRepository = checkRepository;
        this.aiSummaryService = aiSummaryService;
    }

    /**
     * Registra un nuevo deployment en estado inicial, opcionalmente asignado a un técnico.
     *
     * @param dto datos de alta del deployment
     * @return representación del deployment persistido
     */
    @PostMapping
    public DeploymentResponseDTO createDeployment(@Valid @RequestBody DeploymentRequestDTO dto) {
        return service.createDeployment(dto);
    }

    /**
     * Devuelve el listado de deployments disponibles.
     * Si el usuario autenticado es ADMIN, retorna todos.
     * Si es TECHNICIAN, retorna únicamente los suyos.
     *
     * @param authentication contexto de autenticación
     * @return lista de deployments filtrados según rol
     */
    @GetMapping
    public List<DeploymentResponseDTO> getAllDeployments(Authentication authentication) {
        return service.getDeploymentsForUser(authentication);
    }

    /**
     * Devuelve únicamente los deployments asignados al usuario autenticado.
     *
     * @param authentication contexto de autenticación
     * @return lista de deployments propios
     */
    @GetMapping("/my")
    public List<DeploymentResponseDTO> getMyDeployments(Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("No autenticado");
        }
        return service.getMyDeployments(authentication.getName());
    }

    /**
     * Devuelve los deployments huérfanos que aún no tienen técnico asignado.
     *
     * @return lista de deployments sin asignar
     */
    @GetMapping("/unassigned")
    public List<DeploymentResponseDTO> getUnassignedDeployments() {
        return service.getUnassignedDeployments();
    }

    /**
     * Reasigna un deployment a un técnico. Solo permitido para administradores.
     *
     * @param deploymentId id funcional del deployment
     * @param technicianId id del técnico a asignar
     * @return deployment actualizado
     */
    @PutMapping("/{deploymentId}/assign-technician/{technicianId}")
    @PreAuthorize("hasRole('ADMIN')")
    public DeploymentResponseDTO assignTechnician(
            @PathVariable String deploymentId,
            @PathVariable Long technicianId) {
        return service.assignTechnician(deploymentId, technicianId);
    }

    /**
     * Permite que el técnico autenticado reclame o se autoasigne un deployment.
     */
    @PutMapping("/{deploymentId}/claim")
    public DeploymentResponseDTO claimDeployment(
            @PathVariable String deploymentId,
            Authentication authentication
    ) {
        if (authentication == null) {
            throw new AccessDeniedException("No autenticado");
        }
        return service.claimDeployment(deploymentId, authentication.getName());
    }

    /**
     * Consulta un deployment por su identificador funcional.
     *
     * @param deploymentId id funcional del deployment
     * @return deployment encontrado
     */
    @GetMapping("/{deploymentId}")
    public DeploymentResponseDTO getDeploymentByDeploymentId(@PathVariable String deploymentId) {
        return service.getDeploymentByDeploymentId(deploymentId);
    }

    /**
     * Permite modificar manualmente el estado del despliegue, compliance o fase actual.
     *
     * @param deploymentId id funcional del deployment
     * @param dto datos de estado a modificar
     * @param authentication contexto del usuario autenticado
     * @return deployment actualizado
     */
    @PutMapping("/{deploymentId}/status")
    public DeploymentResponseDTO updateDeploymentStatus(
            @PathVariable String deploymentId,
            @RequestBody UpdateDeploymentStatusRequestDTO dto,
            Authentication authentication
    ) {
        Deployment deployment = service.getDeploymentEntityByDeploymentId(deploymentId);
        validateDeploymentAccess(deployment, authentication);

        String username = authentication != null ? authentication.getName() : "system";
        return service.updateDeploymentStatus(deploymentId, dto, username);
    }

    /**
     * Genera y descarga el informe de despliegue en formato PDF.
     * Administradores pueden descargar cualquier informe; los técnicos solo los de sus despliegues asignados.
     */
    @GetMapping("/{deploymentId}/report.pdf")
    public ResponseEntity<byte[]> downloadDeploymentReport(
            @PathVariable String deploymentId,
            Authentication authentication
    ) {
        Deployment deployment = service.getDeploymentEntityByDeploymentId(deploymentId);
        validateDeploymentAccess(deployment, authentication);

        List<DeploymentStep> steps = stepRepository.findByDeploymentOrderByReportedAtAsc(deployment);
        List<SecurityCheck> checks = checkRepository.findByDeploymentOrderByReportedAtAsc(deployment);

        byte[] pdfBytes = reportService.generateDeploymentReport(deployment, steps, checks);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-" + deploymentId + ".pdf\"")
                .body(pdfBytes);
    }

    /**
     * Genera un nuevo análisis de incidencias y recomendaciones mediante IA para el despliegue.
     */
    @PostMapping("/{deploymentId}/ai-summary")
    public ResponseEntity<AiSummaryDTO> generateAiSummary(
            @PathVariable String deploymentId,
            Authentication authentication
    ) {
        Deployment deployment = service.getDeploymentEntityByDeploymentId(deploymentId);
        validateDeploymentAccess(deployment, authentication);

        AiSummaryDTO summary = aiSummaryService.generateSummary(deploymentId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Consulta el último análisis de IA generado para el despliegue.
     */
    @GetMapping("/{deploymentId}/ai-summary")
    public ResponseEntity<AiSummaryDTO> getLatestAiSummary(
            @PathVariable String deploymentId,
            Authentication authentication
    ) {
        Deployment deployment = service.getDeploymentEntityByDeploymentId(deploymentId);
        validateDeploymentAccess(deployment, authentication);

        return aiSummaryService.getLatestSummary(deploymentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(aiSummaryService.generateSummary(deploymentId)));
    }

    private void validateDeploymentAccess(Deployment deployment, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return; // Si no hay autenticación requerida en este endpoint (por SecurityConfig), continuar
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_API_CLIENT".equals(a.getAuthority()));
        if (isAdmin) {
            return;
        }

        boolean isTechnician = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_TECHNICIAN".equals(a.getAuthority()));

        if (isTechnician) {
            if (deployment.getTechnician() == null || !authentication.getName().equals(deployment.getTechnician().getUsername())) {
                throw new AccessDeniedException("No tiene permisos para acceder al despliegue asignado a otro técnico");
            }
        }
    }
}
