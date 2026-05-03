package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.model.Alert;
import com.roblesdavid.blue_deploy.model.Check;
import com.roblesdavid.blue_deploy.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio encargado de analizar checks y generar alertas de seguridad.
 */
@Service
public class AlertService {
    private final AlertRepository repository;

    public AlertService(AlertRepository repository){
        this.repository = repository;
    }

    /**
     * Analiza un check y genera alertas según reglas definidas
     */
    public void analyze(Check check){
        if (Boolean.FALSE.equals(check.getFirewallEnabled())){
            save(check, "FIREWALL_DISABLED", "HIGH");
        }

        if (Boolean.FALSE.equals(check.getAntivirusEnabled())){
            save(check, "ANTIVIRUS_DISABLED", "HIGH");
        }

        if(Boolean.FALSE.equals(check.getDiskEncrypted())){
            save(check, "DISK_NOT_ENCRYPTED", "MEDIUM");
        }

        if(Boolean.FALSE.equals((check.getUpdatesEnabled()))){
            save(check, "UPDATES_DISABLED", "MEDIUM");
        }
    }

    /**
     * Crea y guarda una alerta en base de datos.
     */
    private void save(Check check, String type, String severity){
        Alert alert = new Alert();
        alert.setHostname(check.getHostname());
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setCreatedAt(LocalDateTime.now());

        repository.save(alert);
    }
}
