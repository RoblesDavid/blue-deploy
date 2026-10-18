package com.roblesdavid.blue_deploy.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO de salida con la información agregada de un deployment.
 */
public class DeploymentResponseDTO {

    public Long id;
    public String deploymentId;
    public String hostname;
    public String deviceModel;
    public String domainName;
    public String currentStep;
    public String deploymentStatus;
    public String complianceStatus;
    public String startedAt;
    public String finishedAt;
    public String lastSeenAt;
    public Long technicianId;
    public String technicianName;
    public List<DeploymentStepResponseDTO> steps = new ArrayList<>();
    public List<SecurityCheckResponseDTO> checks = new ArrayList<>();
}
