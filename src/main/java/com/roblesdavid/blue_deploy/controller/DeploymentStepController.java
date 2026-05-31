package com.roblesdavid.blue_deploy.controller;


import com.roblesdavid.blue_deploy.dto.DeploymentStepRequestDTO;
import com.roblesdavid.blue_deploy.dto.DeploymentStepResponseDTO;
import com.roblesdavid.blue_deploy.service.DeploymentStepService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para reportar y consultar pasos de un deployment.
 */
@RestController
@RequestMapping("/api/deployments/{deploymentId}/steps")
public class DeploymentStepController {

    private final DeploymentStepService service;

    public DeploymentStepController(DeploymentStepService service) {
        this.service = service;
    }

    /**
     * Registra la ejecución de una fase del deployment.
     *
     * @param deploymentId identificador del deployment padre
     * @param dto datos del paso reportado
     * @return paso persistido en formato de respuesta
     */
    @PostMapping
    public DeploymentStepResponseDTO registerStep(
            @PathVariable String deploymentId,
            @Valid @RequestBody DeploymentStepRequestDTO dto
    ) {
        return service.registerStep(deploymentId, dto);
    }

    /**
     * Obtiene la traza cronológica de pasos de un deployment.
     *
     * @param deploymentId identificador del deployment
     * @return pasos ordenados por fecha de reporte
     */
    @GetMapping
    public List<DeploymentStepResponseDTO> getStepsByDeploymentId(@PathVariable String deploymentId) {
        return service.getStepsByDeploymentId(deploymentId);
    }
}
