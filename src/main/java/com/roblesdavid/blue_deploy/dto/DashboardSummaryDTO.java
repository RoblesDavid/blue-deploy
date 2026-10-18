package com.roblesdavid.blue_deploy.dto;

/**
 * DTO de salida que resume el estado del dashboard de deployments.
 * Soporta métricas globales para administradores y métricas acotadas para técnicos.
 */
public class DashboardSummaryDTO {

    public long totalDeployments;
    public long inProgress;
    public long domainJoined;
    public long postDomainPending;
    public long completed;
    public long completedWithWarnings;
    public long failed;
    public long nonCompliant;
    public long pendingCrowdStrike;
    public long pendingMicroClaudia;

    // Métricas analíticas añadidas
    public Long assignedTechnicians;
    public Long activeTechnicians;
    public Double averageDeploymentTimeMinutes;
    public String lastFailedDeployment;
    public String lastSecurityWarning;
}
