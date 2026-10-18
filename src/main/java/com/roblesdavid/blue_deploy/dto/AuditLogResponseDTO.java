package com.roblesdavid.blue_deploy.dto;

import com.roblesdavid.blue_deploy.model.AuditAction;
import java.time.LocalDateTime;

/**
 * DTO para consultar eventos del log de auditoría.
 */
public class AuditLogResponseDTO {

    private Long id;
    private AuditAction action;
    private String actorUsername;
    private String targetId;
    private String targetType;
    private String details;
    private String clientIp;
    private LocalDateTime createdAt;

    public AuditLogResponseDTO() {
    }

    public AuditLogResponseDTO(Long id, AuditAction action, String actorUsername, String targetId, String targetType, String details, String clientIp, LocalDateTime createdAt) {
        this.id = id;
        this.action = action;
        this.actorUsername = actorUsername;
        this.targetId = targetId;
        this.targetType = targetType;
        this.details = details;
        this.clientIp = clientIp;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
