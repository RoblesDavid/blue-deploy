package com.roblesdavid.blue_deploy.integration;

import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.UserRole;
import com.roblesdavid.blue_deploy.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AiSummaryIntegrationTest extends BaseApiIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;

    @BeforeEach
    void setUpUsers() {
        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setName("Admin Root");
        admin.setEmail("admin@blue.local");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        appUserRepository.save(admin);

        adminToken = jwtService.generateToken(Map.of("role", "ADMIN"), "admin");
    }

    @Test
    void generateAiSummary_withFailedStepsAndChecks_shouldReturnDiagnosisAndRecommendations() throws Exception {
        String depId = "DEPLOY-AI-ERR";
        createDeployment(depId).andExpect(status().isOk());

        // Registrar paso fallido de unión al dominio
        mockMvc.perform(post("/api/deployments/{deploymentId}/steps", depId)
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stepName\":\"DOMAIN_JOIN\",\"stepStatus\":\"FAILED\",\"errorCode\":\"0x8007052E\",\"message\":\"Error de credenciales o Kerberos\"}"))
                .andExpect(status().isOk());

        // Registrar check fallido de EDR CrowdStrike
        mockMvc.perform(post("/api/deployments/{deploymentId}/checks", depId)
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"checkName\":\"CROWDSTRIKE_VALIDATED\",\"checkStatus\":\"FAILED\",\"severity\":\"CRITICAL\",\"message\":\"Sensor no reporta conexión al CID\"}"))
                .andExpect(status().isOk());

        // Generar análisis con IA
        mockMvc.perform(post("/api/deployments/{deploymentId}/ai-summary", depId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value(depId))
                .andExpect(jsonPath("$.severity").value("CRITICAL"))
                .andExpect(jsonPath("$.summary", containsString("Active Directory")))
                .andExpect(jsonPath("$.probableCause", containsString("DNS/Kerberos")))
                .andExpect(jsonPath("$.recommendedActions", hasSize(greaterThanOrEqualTo(2))));

        // Consultar el análisis generado mediante GET
        mockMvc.perform(get("/api/deployments/{deploymentId}/ai-summary", depId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value(depId))
                .andExpect(jsonPath("$.severity").value("CRITICAL"));
    }
}
