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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bluedeploy;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect", // <-- Fuerza la resolución del dialecto
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true",
        "bluedeploy.api-key=test-api-key-12345"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeploymentFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Parser de JSON de Spring

    // ID Unificado idéntico para toda la simulación del ciclo
    private static final String DEPLOYMENT_ID = "DEPLOY-AGSNEG-QEMU-TEST-20260524";
    private static final String API_KEY_HEADER = "X-BlueDeploy-Api-Key";
    private static final String TEST_API_KEY = "test-api-key-12345";

    @Test
    @Order(1)
    void fase000_deberiaInicializarDeploymentCorrectamente() throws Exception {
        // Simula lo que genera "01 - Start-OSDCloud.ps1" en WinPE
        Map<String, Object> payload = new HashMap<>();
        payload.put("deploymentId", DEPLOYMENT_ID);
        payload.put("hostname", "QEMU-TEST");
        payload.put("status", "STARTING");
        payload.put("message", "Inicio del proceso de despliegue automatizado OSDCloud en WinPE");

        mockMvc.perform(post("/api/deployments")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk()) // o isCreated() según tu controlador
                .andExpect(jsonPath("$.deploymentId").value(DEPLOYMENT_ID));
    }

    @Test
    @Order(2)
    void fase030_deberiaRegistrarPasoSetupComplete() throws Exception {
        // Simula la llamada del paso 030
        Map<String, Object> payload = new HashMap<>();
        payload.put("stepName", "SETUPCOMPLETE_EXECUTED");
        payload.put("stepStatus", "SUCCESS");
        payload.put("message", "SetupComplete script ran successfully");
        payload.put("errorCode", null); // Validamos que el backend asimile el nulo de .NET sin dar Error 400
        payload.put("rawOutput", "AutoLogon configured");

        mockMvc.perform(post("/api/deployments/" + DEPLOYMENT_ID + "/steps")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void fase100_deberiaRegistrarPasoUnionDominio() throws Exception {
        // Simula el paso 100 (Unión al dominio con los strings limpios)
        Map<String, Object> payload = new HashMap<>();
        payload.put("stepName", "JOIN_DOMAIN_EXECUTION");
        payload.put("stepStatus", "SUCCESS");
        payload.put("message", "The computer successfully joined the active directory domain structure");
        payload.put("errorCode", null);
        payload.put("rawOutput", "Domain: dmsas.sda.sas.junta-andalucia.es");

        mockMvc.perform(post("/api/deployments/" + DEPLOYMENT_ID + "/steps")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void fase145_deberiaRegistrarCheckDeSeguridad() throws Exception {
        // Simula lo que envía nuestro nuevo script de Microclaudia, CrowdStrike y AutoLogon
        Map<String, Object> payload = new HashMap<>();
        payload.put("checkName", "FINAL_SECURITY_COMPLIANCE_AUDIT");
        payload.put("checkStatus", "PASSED");
        payload.put("severity", "LOW");
        payload.put("expectedValue", "Microclaudia=True; CrowdStrike=Running; AutoAdminLogon=Wiped");
        payload.put("actualValue", "Microclaudia=True; CrowdStrike=True; Wiped=True");
        payload.put("message", "All mandatory security endpoint agents are installed");

        // Apunta al endpoint específico de validaciones "/checks"
        mockMvc.perform(post("/api/deployments/" + DEPLOYMENT_ID + "/checks")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void faseErrores_deberiaDar404SiElDeploymentNoExiste() throws Exception {
        // Prueba de robustez: Comprobamos que si un script envía un paso con un ID roto,
        // la API responda con el 404 que capturamos en los logs de PowerShell.
        String idInexistente = "DEPLOY-ID-FALSO-999999";
        Map<String, Object> payload = new HashMap<>();
        payload.put("stepName", "WINDOWS_INSTALLATION");
        payload.put("stepStatus", "SUCCESS");

        mockMvc.perform(post("/api/deployments/" + idInexistente + "/steps")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound()); // Verifica que el backend lance el 404 controlado
    }
}
