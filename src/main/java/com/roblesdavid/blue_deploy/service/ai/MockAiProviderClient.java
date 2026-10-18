package com.roblesdavid.blue_deploy.service.ai;

import com.roblesdavid.blue_deploy.model.CheckStatus;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStep;
import com.roblesdavid.blue_deploy.model.SecurityCheck;
import com.roblesdavid.blue_deploy.model.StepStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Proveedor Mock y Heurístico para análisis de errores de despliegue mediante IA.
 * Analiza fallos de OSDCloud, Active Directory, drivers, Radius y compliance EDR.
 */
@Component
public class MockAiProviderClient implements AiProviderClient {

    @Override
    public AiAnalysisResult analyzeDeployment(Deployment deployment, List<DeploymentStep> steps, List<SecurityCheck> checks) {
        List<DeploymentStep> failedSteps = steps.stream()
                .filter(s -> s.getStepStatus() == StepStatus.FAILED)
                .toList();

        List<SecurityCheck> nonCompliantChecks = checks.stream()
                .filter(c -> c.getCheckStatus() == CheckStatus.FAILED || c.getCheckStatus() == CheckStatus.WARNING)
                .toList();

        if (failedSteps.isEmpty() && nonCompliantChecks.isEmpty()) {
            return new AiAnalysisResult(
                    "El despliegue en el equipo '" + deployment.getHostname() + "' se completó sin incidencias críticas registradas.",
                    "No se detectaron errores en las fases OSDCloud ni incumplimientos en los checks de seguridad.",
                    List.of(
                            "Verificar telemetría final en consola EDR (CrowdStrike / MicroClaudia).",
                            "Archivar despliegue o asignar a usuario final conforme al protocolo."
                    ),
                    "LOW"
            );
        }

        StringBuilder summaryBuilder = new StringBuilder();
        StringBuilder causeBuilder = new StringBuilder();
        List<String> actions = new ArrayList<>();
        String severity = "MEDIUM";

        // Análisis de pasos fallidos
        for (DeploymentStep step : failedSteps) {
            String stepName = step.getStepName() != null ? step.getStepName().toUpperCase() : "";
            String errorCode = step.getErrorCode() != null ? step.getErrorCode() : "SIN_CODIGO";

            if (stepName.contains("DOMAIN_JOIN") || stepName.contains("AD") || stepName.contains("JOIN")) {
                severity = "HIGH";
                summaryBuilder.append("Fallo crítico en la unión al dominio Active Directory (fase ").append(step.getStepName()).append("). ");
                causeBuilder.append("Posible falta de conectividad DNS/Kerberos con el Controlador de Dominio o credenciales de unión delegadas inválidas (Error: ").append(errorCode).append("). ");
                actions.add("Comprobar conectividad DNS contra los servidores DNS corporativos.");
                actions.add("Validar permisos de la cuenta de servicio de unión al dominio en la OU correspondiente.");
                actions.add("Verificar sincronización NTP horaria en el cliente (diferencia menor a 5 minutos).");
            } else if (stepName.contains("RADIUS") || stepName.contains("8021X") || stepName.contains("WIFI")) {
                if (!"CRITICAL".equals(severity)) severity = "HIGH";
                summaryBuilder.append("Incidencia en configuración de autenticación de red 802.1X/Radius. ");
                causeBuilder.append("Falta de certificado raíz CA corporativo o perfil de red supplicant rechazado por NPS/Radius. ");
                actions.add("Comprobar despliegue de certificados de CA intermedia y raíz mediante GPO/SCEP.");
                actions.add("Revisar registros de eventos en servidor NPS/FreeRADIUS para la MAC del dispositivo.");
            } else if (stepName.contains("DRIVER") || stepName.contains("SOFTWARE") || stepName.contains("INSTALL")) {
                summaryBuilder.append("Fallo en aprovisionamiento de paquetes/controladores en '").append(step.getStepName()).append("'. ");
                causeBuilder.append("Paquete instalador no soportado para el modelo '").append(deployment.getDeviceModel()).append("' o timeout en repositorio local. ");
                actions.add("Verificar repositorio OSDCloud de drivers para el modelo " + deployment.getDeviceModel() + ".");
                actions.add("Ejecutar instalación manual en entorno de pruebas y revisar log C:\\Windows\\Logs\\SetupComplete.log.");
            } else {
                summaryBuilder.append("Fallo en la fase '").append(step.getStepName()).append("' con código ").append(errorCode).append(". ");
                causeBuilder.append("Interrupción en script post-instalación: ").append(step.getMessage() != null ? step.getMessage() : "Error genérico").append(". ");
                actions.add("Revisar log detallado en el host en C:\\tmp\\BlueDeploy\\Logs.");
            }
        }

        // Análisis de checks de seguridad no conformes
        for (SecurityCheck check : nonCompliantChecks) {
            String checkName = check.getCheckName() != null ? check.getCheckName().toUpperCase() : "";

            if (checkName.contains("CROWDSTRIKE") || checkName.contains("MICROCLAUDIA") || checkName.contains("EDR") || checkName.contains("ANTIVIRUS")) {
                severity = "CRITICAL";
                summaryBuilder.append("Incidencia de seguridad en agente EDR/Antivirus (").append(check.getCheckName()).append("). ");
                causeBuilder.append("El agente EDR no reporta conectividad con la nube o no se inició el servicio del sensor. ");
                actions.add("Verificar servicio de sensor CrowdStrike / MicroClaudia y Connectivity CID en el cliente.");
                actions.add("Aislar provisionalmente el equipo en red de cuarentena hasta validar el sensor.");
            } else if (checkName.contains("BITLOCKER") || checkName.contains("ENCRYPTION")) {
                if (!"CRITICAL".equals(severity)) severity = "HIGH";
                summaryBuilder.append("Cifrado BitLocker no activo o clave de recuperación no respaldada en AD/Intune. ");
                causeBuilder.append("TPM no inicializado o política de cifrado rechazada por hardware. ");
                actions.add("Comprobar estado del chip TPM 2.0 en BIOS/UEFI.");
                actions.add("Forzar cifrado de volumen mediante 'Enable-BitLocker' con respaldo en AD.");
            } else if (checkName.contains("ADMIN") || checkName.contains("LAPS") || checkName.contains("PASSWORD")) {
                if (!"CRITICAL".equals(severity)) severity = "HIGH";
                summaryBuilder.append("Riesgo de escalada o configuración de administrador local no conforme. ");
                causeBuilder.append("Cuenta de administrador local habilitada o cliente LAPS no sincronizado. ");
                actions.add("Verificar rotación de contraseñas de administrador local con Windows LAPS.");
            } else {
                summaryBuilder.append("Check no conforme: ").append(check.getCheckName()).append(". ");
                causeBuilder.append(check.getMessage() != null ? check.getMessage() : "Valor obtenido fuera de la línea base").append(". ");
                actions.add("Revisar valor obtenido: '" + check.getActualValue() + "' frente al esperado: '" + check.getExpectedValue() + "'.");
            }
        }

        if (actions.isEmpty()) {
            actions.add("Revisar logs de auditoría y contactar al administrador del sistema.");
        }

        return new AiAnalysisResult(
                summaryBuilder.toString().trim(),
                causeBuilder.toString().trim(),
                actions,
                severity
        );
    }
}
