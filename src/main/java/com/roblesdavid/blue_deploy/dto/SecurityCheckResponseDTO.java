package com.roblesdavid.blue_deploy.dto;

/**
 * DTO de salida para devolver checks de seguridad reportados.
 */
public class SecurityCheckResponseDTO {
    public Long id;
    public String deploymentId;
    public String checkName;
    public String checkStatus;
    public String severity;
    public String expectedValue;
    public String actualValue;
    public String message;
    public String checkedAt;
    public String reportedAt;
}
