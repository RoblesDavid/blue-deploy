package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.AuditLogResponseDTO;
import com.roblesdavid.blue_deploy.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para consultar registros de auditoría (exclusivo para rol ADMIN).
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponseDTO>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }

    @GetMapping("/deployments/{deploymentId}")
    public ResponseEntity<List<AuditLogResponseDTO>> getLogsByDeployment(@PathVariable String deploymentId) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByTarget(deploymentId));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<AuditLogResponseDTO>> getLogsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByTarget(userId));
    }
}
