package com.roblesdavid.blue_deploy.controller;


import com.roblesdavid.blue_deploy.dto.DeploymentStepRequestDTO;
import com.roblesdavid.blue_deploy.dto.DeploymentStepResponseDTO;
import com.roblesdavid.blue_deploy.service.DeploymentStepService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deployments/{deploymentId}/steps")
public class DeploymentStepController {

    private final DeploymentStepService service;

    public DeploymentStepController(DeploymentStepService service) {
        this.service = service;
    }

    @PostMapping
    public DeploymentStepResponseDTO registerStep(
            @PathVariable String deploymentId,
            @Valid @RequestBody DeploymentStepRequestDTO dto
    ) {
        return service.registerStep(deploymentId, dto);
    }

    @GetMapping
    public List<DeploymentStepResponseDTO> getStepsByDeploymentId(@PathVariable String deploymentId) {
        return service.getStepsByDeploymentId(deploymentId);
    }
}
