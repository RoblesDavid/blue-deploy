package com.roblesdavid.blue_deploy.dto;

import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para recibir checks del sistema.
 * Contiene validaciones básicas de integridad.
 */
public class CheckRequestDTO {

    /**
     * Fecha en formato ISO básico: yyyy-MM-dd HH:mm:ss
     */
    @NotBlank(message = "date no puede estar vacío")
    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
            message = "Formato de fecha inválido (yyyy-MM-dd HH:mm:ss)"
    )
    private String date;

    @NotBlank(message = "use no puede estar vacío")
    private String user;
    @NotBlank(message = "hostname no puede estar vacío")
    private String hostname;
    @NotNull(message = "checks no puede ser null")
    private Map<String, Object> checks;

    //Getter & Setter
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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
