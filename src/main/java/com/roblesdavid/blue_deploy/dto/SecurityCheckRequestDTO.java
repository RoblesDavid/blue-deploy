package com.roblesdavid.blue_deploy.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.roblesdavid.blue_deploy.model.CheckStatus;
import com.roblesdavid.blue_deploy.model.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO de entrada para reportar una comprobación de seguridad.
 */
public class SecurityCheckRequestDTO {

    @NotBlank(message = "checkName no puede estar vacío")
    @JsonAlias({"name", "checkType", "check_type", "check_name"})
    private String checkName;

    @NotNull(message = "checkStatus no puede ser null")
    @JsonAlias({"status", "state", "check_status"})
    private CheckStatus checkStatus;

    @NotNull(message = "severity no puede ser null")
    private Severity severity;

    private String expectedValue;

    private String actualValue;

    private String message;

    private LocalDateTime checkedAt;

    public String getCheckName() {
        return checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    public CheckStatus getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(CheckStatus checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }
}
