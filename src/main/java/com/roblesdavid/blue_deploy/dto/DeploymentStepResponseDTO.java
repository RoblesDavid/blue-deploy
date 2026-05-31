package com.roblesdavid.blue_deploy.dto;

/**
 * DTO de salida para exponer una fase reportada de deployment.
 */
public class DeploymentStepResponseDTO {

    public Long id;
    public String deploymentId;
    public String stepName;
    public String stepStatus;
    public String message;
    public String errorCode;
    public String rawOutput;
    public String startedAt;
    public String finishedAt;
    public String reportedAt;
}
