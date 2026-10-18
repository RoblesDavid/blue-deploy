package com.roblesdavid.blue_deploy.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferir el resumen inteligente de incidencias.
 */
public class AiSummaryDTO {

    private Long id;
    private String deploymentId;
    private String summary;
    private String probableCause;
    private List<String> recommendedActions;
    private String severity;
    private LocalDateTime createdAt;

    public AiSummaryDTO() {
    }

    public AiSummaryDTO(Long id, String deploymentId, String summary, String probableCause, List<String> recommendedActions, String severity, LocalDateTime createdAt) {
        this.id = id;
        this.deploymentId = deploymentId;
        this.summary = summary;
        this.probableCause = probableCause;
        this.recommendedActions = recommendedActions;
        this.severity = severity;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getProbableCause() {
        return probableCause;
    }

    public void setProbableCause(String probableCause) {
        this.probableCause = probableCause;
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
