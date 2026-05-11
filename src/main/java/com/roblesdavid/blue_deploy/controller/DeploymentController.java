package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.DeploymentRequestDTO;
import com.roblesdavid.blue_deploy.dto.DeploymentResponseDTO;
import com.roblesdavid.blue_deploy.service.DeploymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deployments")
public class DeploymentController {

    private final DeploymentService service;

    public DeploymentController(DeploymentService service) {
        this.service = service;
    }

    @PostMapping
    public DeploymentResponseDTO createDeployment(@Valid @RequestBody DeploymentRequestDTO dto) {
        return service.createDeployment(dto);
    }

    @GetMapping
    public List<DeploymentResponseDTO> getAllDeployments() {
        return service.getAllDeployments();
    }

    @GetMapping("/{deploymentId}")
    public DeploymentResponseDTO getDeploymentByDeploymentId(@PathVariable String deploymentId) {
        return service.getDeploymentByDeploymentId(deploymentId);
    }
}