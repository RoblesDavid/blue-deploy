package com.roblesdavid.blue_deploy.dto;

/**
 * Payload para actualización manual del estado, compliance o fase de un despliegue.
 */
public class UpdateDeploymentStatusRequestDTO {

    private String deploymentStatus;
    private String complianceStatus;
    private String currentStep;
    private String note;

    public UpdateDeploymentStatusRequestDTO() {
    }

    public UpdateDeploymentStatusRequestDTO(String deploymentStatus, String complianceStatus, String currentStep, String note) {
        this.deploymentStatus = deploymentStatus;
        this.complianceStatus = complianceStatus;
        this.currentStep = currentStep;
        this.note = note;
    }

    public String getDeploymentStatus() {
        return deploymentStatus;
    }

    public void setDeploymentStatus(String deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
    }

    public String getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(String complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
