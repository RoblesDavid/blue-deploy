package com.roblesdavid.blue_deploy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que almacena el resumen y diagnóstico inteligente de incidencias para un despliegue.
 */
@Entity
@Table(name = "ai_summaries", indexes = {
        @Index(name = "idx_ai_summary_deployment_id", columnList = "deployment_id"),
        @Index(name = "idx_ai_summary_created_at", columnList = "created_at")
})
public class AiSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deployment_id", nullable = false, length = 100)
    private String deploymentId;

    @Column(length = 2000, nullable = false)
    private String summary;

    @Column(name = "probable_cause", length = 2000)
    private String probableCause;

    @Column(name = "recommended_actions", length = 3000)
    private String recommendedActions;

    @Column(length = 50, nullable = false)
    private String severity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AiSummary() {
    }

    public AiSummary(String deploymentId, String summary, String probableCause, String recommendedActions, String severity) {
        this.deploymentId = deploymentId;
        this.summary = summary;
        this.probableCause = probableCause;
        this.recommendedActions = recommendedActions;
        this.severity = severity;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
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

    public String getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(String recommendedActions) {
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
