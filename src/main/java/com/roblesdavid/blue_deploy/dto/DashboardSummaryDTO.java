package com.roblesdavid.blue_deploy.dto;

/**
 * DTO de salida que resume el estado global del dashboard de deployments.
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
}
