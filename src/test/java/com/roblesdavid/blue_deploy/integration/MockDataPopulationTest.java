package com.roblesdavid.blue_deploy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Populates H2 with realistic multi-technician deployment data so the dashboard
 * endpoint returns meaningful numbers during manual frontend testing.
 *
 * Scenario:
 *   Carlos Rodríguez (2 deployments): COMPLETED + FAILED
 *   Ana García        (2 deployments): COMPLETED + NON_COMPLIANT
 *
 * Expected summary after all tests:
 *   totalDeployments=4, completed=1, completedWithWarnings=1 (approx — depends
 *   on compliance evaluation), failed=1, nonCompliant=1
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mockdata;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "bluedeploy.api-key=test-api-key-12345"
})
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MockDataPopulationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ─── Carlos Rodríguez ─────────────────────────────────────────────────────

    /** Deployment 1 (Carlos): completes successfully with all security checks PASSED */
    @Test
    @Order(10)
    void carlos_deploy01_inicializar() throws Exception {
        performPost("/api/deployments", Map.of(
                "deploymentId", "DEPLOY-VENTAS-001-TEST",
                "hostname", "PC-VENTAS-001",
                "deviceModel", "Dell Latitude 5440",
                "domainName", "corp.local"
        )).andExpect(status().isOk())
          .andExpect(jsonPath("$.deploymentId").value("DEPLOY-VENTAS-001-TEST"));
    }

    @Test
    @Order(11)
    void carlos_deploy01_fases_exitosas() throws Exception {
        registrarPaso("DEPLOY-VENTAS-001-TEST", "HOSTNAME_CAPTURED", "SUCCESS", "Hostname capturado", null);
        registrarPaso("DEPLOY-VENTAS-001-TEST", "DRIVER_INSTALLATION", "SUCCESS", "Drivers Dell instalados", null);
        registrarPaso("DEPLOY-VENTAS-001-TEST", "RADIUS_8021X_CONFIGURATION", "SUCCESS", "Perfil 802.1X aplicado", null);
        registrarPaso("DEPLOY-VENTAS-001-TEST", "DOMAIN_JOIN", "SUCCESS", "Equipo unido al dominio corp.local", null);
        registrarPaso("DEPLOY-VENTAS-001-TEST", "GPUPDATE", "SUCCESS", "Politicas de grupo aplicadas", null);
    }

    @Test
    @Order(12)
    void carlos_deploy01_checks_seguridad() throws Exception {
        registrarCheck("DEPLOY-VENTAS-001-TEST", "FIREWALL_ENABLED", "PASSED", "HIGH", "Enabled", "Enabled", "Windows Defender Firewall activo");
        registrarCheck("DEPLOY-VENTAS-001-TEST", "WSUS_CONFIGURED", "PASSED", "MEDIUM", "WSUS server configurado", "WSUS server configurado", "WSUS corporativo OK");
        registrarCheck("DEPLOY-VENTAS-001-TEST", "CROWDSTRIKE_VALIDATED", "PASSED", "CRITICAL", "Running", "Running", "CrowdStrike Falcon activo y reportando");
        registrarCheck("DEPLOY-VENTAS-001-TEST", "MICROCLAUDIA_VALIDATED", "PASSED", "CRITICAL", "Installed", "Installed", "MicroClaudia instalada correctamente");
        registrarCheck("DEPLOY-VENTAS-001-TEST", "LOCAL_ADMIN_VALIDATED", "PASSED", "HIGH", "AutoAdminLogon=0", "AutoAdminLogon=0", "Autologon eliminado");
    }

    @Test
    @Order(13)
    void carlos_deploy01_finalizar_y_validar() throws Exception {
        registrarPaso("DEPLOY-VENTAS-001-TEST", "FINAL_SECURITY_VALIDATION", "SUCCESS", "Validacion de seguridad superada", null);

        mockMvc.perform(get("/api/deployments/DEPLOY-VENTAS-001-TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.complianceStatus").value("COMPLIANT"));
    }

    /** Deployment 2 (Carlos): fails at driver installation */
    @Test
    @Order(20)
    void carlos_deploy02_inicializar() throws Exception {
        performPost("/api/deployments", Map.of(
                "deploymentId", "DEPLOY-RRHH-002-TEST",
                "hostname", "PC-RRHH-002",
                "deviceModel", "Lenovo ThinkPad T490",
                "domainName", "corp.local"
        )).andExpect(status().isOk());
    }

    @Test
    @Order(21)
    void carlos_deploy02_fallo_en_drivers() throws Exception {
        registrarPaso("DEPLOY-RRHH-002-TEST", "HOSTNAME_CAPTURED", "SUCCESS", "Hostname capturado", null);

        performPost("/api/deployments/DEPLOY-RRHH-002-TEST/steps", Map.of(
                "stepName", "DRIVER_INSTALLATION",
                "stepStatus", "FAILED",
                "message", "Paquete de drivers no encontrado para este modelo",
                "errorCode", "DRIVER_PACKAGE_NOT_FOUND",
                "rawOutput", "Model 'Lenovo ThinkPad T490' not found in driver catalog"
        )).andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments/DEPLOY-RRHH-002-TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("FAILED"));
    }

    // ─── Ana García ───────────────────────────────────────────────────────────

    /** Deployment 3 (Ana): completes but with a WARNING check (WSUS partially configured) */
    @Test
    @Order(30)
    void ana_deploy03_inicializar() throws Exception {
        performPost("/api/deployments", Map.of(
                "deploymentId", "DEPLOY-SOPORT-003-TEST",
                "hostname", "PC-SOPORT-003",
                "deviceModel", "HP ProBook 450 G9",
                "domainName", "corp.local"
        )).andExpect(status().isOk());
    }

    @Test
    @Order(31)
    void ana_deploy03_fases_exitosas() throws Exception {
        registrarPaso("DEPLOY-SOPORT-003-TEST", "HOSTNAME_CAPTURED", "SUCCESS", "Hostname capturado", null);
        registrarPaso("DEPLOY-SOPORT-003-TEST", "DRIVER_INSTALLATION", "SUCCESS", "Drivers HP instalados", null);
        registrarPaso("DEPLOY-SOPORT-003-TEST", "RADIUS_8021X_CONFIGURATION", "SUCCESS", "Red 802.1X configurada", null);
        registrarPaso("DEPLOY-SOPORT-003-TEST", "DOMAIN_JOIN", "SUCCESS", "Dominio corp.local unido", null);
        registrarPaso("DEPLOY-SOPORT-003-TEST", "GPUPDATE", "SUCCESS", "Politicas aplicadas", null);
    }

    @Test
    @Order(32)
    void ana_deploy03_checks_con_advertencia_wsus() throws Exception {
        registrarCheck("DEPLOY-SOPORT-003-TEST", "FIREWALL_ENABLED", "PASSED", "HIGH", "Enabled", "Enabled", "Firewall activo");
        registrarCheck("DEPLOY-SOPORT-003-TEST", "WSUS_CONFIGURED", "WARNING", "MEDIUM",
                "WSUS server configurado", "WSUS parcialmente configurado",
                "Servidor WSUS responde con latencia elevada (>500ms)");
        registrarCheck("DEPLOY-SOPORT-003-TEST", "CROWDSTRIKE_VALIDATED", "PASSED", "CRITICAL", "Running", "Running", "CrowdStrike activo");
        registrarCheck("DEPLOY-SOPORT-003-TEST", "MICROCLAUDIA_VALIDATED", "PASSED", "CRITICAL", "Installed", "Installed", "MicroClaudia instalada");
    }

    @Test
    @Order(33)
    void ana_deploy03_finalizar_con_advertencia() throws Exception {
        registrarPaso("DEPLOY-SOPORT-003-TEST", "FINAL_SECURITY_VALIDATION", "WARNING",
                "Validacion completada con advertencias en WSUS", null);

        mockMvc.perform(get("/api/deployments/DEPLOY-SOPORT-003-TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("COMPLETED_WITH_WARNINGS"));
    }

    /** Deployment 4 (Ana): non-compliant — CrowdStrike and MicroClaudia FAILED */
    @Test
    @Order(40)
    void ana_deploy04_inicializar() throws Exception {
        performPost("/api/deployments", Map.of(
                "deploymentId", "DEPLOY-DISENO-004-TEST",
                "hostname", "PC-DISENO-004",
                "deviceModel", "Dell Precision 3660",
                "domainName", "corp.local"
        )).andExpect(status().isOk());
    }

    @Test
    @Order(41)
    void ana_deploy04_fases_exitosas() throws Exception {
        registrarPaso("DEPLOY-DISENO-004-TEST", "HOSTNAME_CAPTURED", "SUCCESS", "Hostname capturado", null);
        registrarPaso("DEPLOY-DISENO-004-TEST", "DRIVER_INSTALLATION", "SUCCESS", "Drivers instalados", null);
        registrarPaso("DEPLOY-DISENO-004-TEST", "RADIUS_8021X_CONFIGURATION", "SUCCESS", "Red 802.1X configurada", null);
        registrarPaso("DEPLOY-DISENO-004-TEST", "DOMAIN_JOIN", "SUCCESS", "Dominio unido", null);
        registrarPaso("DEPLOY-DISENO-004-TEST", "GPUPDATE", "SUCCESS", "Politicas aplicadas", null);
    }

    @Test
    @Order(42)
    void ana_deploy04_checks_criticos_fallidos() throws Exception {
        registrarCheck("DEPLOY-DISENO-004-TEST", "FIREWALL_ENABLED", "PASSED", "HIGH", "Enabled", "Enabled", "Firewall activo");
        registrarCheck("DEPLOY-DISENO-004-TEST", "WSUS_CONFIGURED", "PASSED", "MEDIUM", "WSUS server configurado", "WSUS server configurado", "WSUS OK");
        registrarCheck("DEPLOY-DISENO-004-TEST", "CROWDSTRIKE_VALIDATED", "FAILED", "CRITICAL",
                "Running", "Not found",
                "CrowdStrike no detectado tras 2 horas post-dominio. Posible fallo en GPO.");
        registrarCheck("DEPLOY-DISENO-004-TEST", "MICROCLAUDIA_VALIDATED", "FAILED", "CRITICAL",
                "Installed", "Not found",
                "MicroClaudia no detectada. Revisar politica de instalacion de dominio.");
    }

    @Test
    @Order(43)
    void ana_deploy04_validar_no_conforme() throws Exception {
        mockMvc.perform(get("/api/deployments/DEPLOY-DISENO-004-TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complianceStatus").value("NON_COMPLIANT"))
                .andExpect(jsonPath("$.deploymentStatus").value("NON_COMPLIANT"));
    }

    // ─── Dashboard summary validation ─────────────────────────────────────────

    @Test
    @Order(99)
    void dashboard_summary_refleja_todos_los_despliegues() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeployments").value(4))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.nonCompliant").value(1))
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.completedWithWarnings").value(1));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private org.springframework.test.web.servlet.ResultActions performPost(String url, Map<String, Object> body) throws Exception {
        return mockMvc.perform(post(url)
                .header("X-BlueDeploy-Api-Key", "test-api-key-12345")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private void registrarPaso(String deploymentId, String stepName, String stepStatus, String message, String errorCode) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("stepName", stepName);
        payload.put("stepStatus", stepStatus);
        payload.put("message", message);
        payload.put("errorCode", errorCode);
        performPost("/api/deployments/" + deploymentId + "/steps", payload)
                .andExpect(status().isOk());
    }

    private void registrarCheck(String deploymentId, String checkName, String checkStatus,
                                 String severity, String expectedValue, String actualValue, String message) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("checkName", checkName);
        payload.put("checkStatus", checkStatus);
        payload.put("severity", severity);
        payload.put("expectedValue", expectedValue);
        payload.put("actualValue", actualValue);
        payload.put("message", message);
        performPost("/api/deployments/" + deploymentId + "/checks", payload)
                .andExpect(status().isOk());
    }
}
