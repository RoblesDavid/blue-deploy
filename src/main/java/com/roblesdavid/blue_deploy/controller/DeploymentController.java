package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.DeploymentRequestDTO;
import com.roblesdavid.blue_deploy.dto.DeploymentResponseDTO;
import com.roblesdavid.blue_deploy.service.DeploymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones de consulta y alta de deployments.
 */
@RestController
@RequestMapping("/api/deployments")
public class DeploymentController {

    private final DeploymentService service;

    public DeploymentController(DeploymentService service) {
        this.service = service;
    }

    /**
     * Registra un nuevo deployment en estado inicial.
     *
     * @param dto datos de alta del deployment
     * @return representación del deployment persistido
     */
    @PostMapping
    public DeploymentResponseDTO createDeployment(@Valid @RequestBody DeploymentRequestDTO dto) {
        return service.createDeployment(dto);
    }

    /**
     * Devuelve el listado completo de deployments registrados.
     *
     * @return lista de deployments
     */
    @GetMapping
    public List<DeploymentResponseDTO> getAllDeployments() {
        return service.getAllDeployments();
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
}
