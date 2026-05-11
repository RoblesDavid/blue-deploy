package com.roblesdavid.blue_deploy.dto;

import jakarta.validation.constraints.NotBlank;

public class DeploymentRequestDTO {

    @NotBlank(message = "deploymentId no puede estar vacío")
    private String deploymentId;

    @NotBlank(message = "hostname no puede estar vacío")
    private String hostname;

    private String deviceModel;

    private String domainName;

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
}