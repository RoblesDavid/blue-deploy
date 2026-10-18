package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.SecurityCheckRequestDTO;
import com.roblesdavid.blue_deploy.dto.SecurityCheckResponseDTO;
import com.roblesdavid.blue_deploy.service.SecurityCheckService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para checks de seguridad asociados a un deployment.
 */
@RestController
@RequestMapping("/api/deployments/{deploymentId}/checks")
public class SecurityCheckController {

    private final SecurityCheckService service;

    public SecurityCheckController(SecurityCheckService service) {
        this.service = service;
    }

    /**
     * Registra el resultado de una comprobación de seguridad.
     *
     * @param deploymentId identificador del deployment
     * @param dto payload de la comprobación
     * @return check persistido en formato de respuesta
     */
    @PostMapping
    public SecurityCheckResponseDTO registerCheck(
            @PathVariable String deploymentId,
            @Valid @RequestBody SecurityCheckRequestDTO dto
    ) {
        return service.registerCheck(deploymentId, dto);
    }

    /**
     * Permite a un técnico o administrador resolver/remediar una comprobación de seguridad pendiente o fallida.
     *
     * @param deploymentId identificador del deployment
     * @param checkId id de la comprobación
     * @param payload mensaje o nota opcional de resolución
     * @param authentication contexto del usuario autenticado
     * @return check actualizado
     */
    @PutMapping("/{checkId}/resolve")
    public SecurityCheckResponseDTO resolveCheck(
            @PathVariable String deploymentId,
            @PathVariable Long checkId,
            @RequestBody(required = false) Map<String, String> payload,
            Authentication authentication
    ) {
        String message = payload != null ? payload.getOrDefault("message", payload.get("resolutionNote")) : null;
        String actor = authentication != null ? authentication.getName() : "Técnico";
        return service.resolveCheck(deploymentId, checkId, message, actor);
    }

    /**
     * Recupera los checks de seguridad de un deployment.
     *
     * @param deploymentId identificador del deployment
     * @return checks ordenados por fecha de reporte
     */
    @GetMapping
    public List<SecurityCheckResponseDTO> getChecksByDeploymentId(@PathVariable String deploymentId) {
        return service.getChecksByDeploymentId(deploymentId);
    }
}
