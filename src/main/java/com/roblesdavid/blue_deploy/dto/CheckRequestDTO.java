package com.roblesdavid.blue_deploy.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para recibir checks del sistema.
 * Contiene validaciones básicas de integridad.
 */
public class CheckRequestDTO {


    private OffsetDateTime date;

    @NotBlank(message = "use no puede estar vacío")
    private String user;
    @NotBlank(message = "hostname no puede estar vacío")
    private String hostname;
    @NotNull(message = "checks no puede ser null")
    private Map<String, Object> checks;

    //Getter & Setter


    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Map<String, Object> getChecks() {
        return checks;
    }

    public void setChecks(Map<String, Object> checks) {
        this.checks = checks;
    }
}
