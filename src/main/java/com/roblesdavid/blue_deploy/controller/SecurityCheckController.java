package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.SecurityCheckRequestDTO;
import com.roblesdavid.blue_deploy.dto.SecurityCheckResponseDTO;
import com.roblesdavid.blue_deploy.service.SecurityCheckService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deployments/{deploymentId}/checks")
public class SecurityCheckController {

    private final SecurityCheckService service;

    public SecurityCheckController(SecurityCheckService service) {
        this.service = service;
    }

    @PostMapping
    public SecurityCheckResponseDTO registerCheck(
            @PathVariable String deploymentId,
            @Valid @RequestBody SecurityCheckRequestDTO dto
    ) {
        return service.registerCheck(deploymentId, dto);
    }

    @GetMapping
    public List<SecurityCheckResponseDTO> getChecksByDeploymentId(@PathVariable String deploymentId) {
        return service.getChecksByDeploymentId(deploymentId);
    }
}
