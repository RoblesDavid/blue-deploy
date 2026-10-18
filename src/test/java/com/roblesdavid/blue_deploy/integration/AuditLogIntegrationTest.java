package com.roblesdavid.blue_deploy.integration;

import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.AuditAction;
import com.roblesdavid.blue_deploy.model.UserRole;
import com.roblesdavid.blue_deploy.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuditLogIntegrationTest extends BaseApiIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String techToken;

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

        AppUser tech = new AppUser();
        tech.setUsername("tech1");
        tech.setPassword(passwordEncoder.encode("tech123"));
        tech.setName("Técnico Uno");
        tech.setEmail("tech1@blue.local");
        tech.setRole(UserRole.TECHNICIAN);
        tech.setActive(true);
        appUserRepository.save(tech);

        adminToken = jwtService.generateToken(Map.of("role", "ADMIN"), "admin");
        techToken = jwtService.generateToken(Map.of("role", "TECHNICIAN"), "tech1");
    }

    @Test
    void login_success_and_failure_shouldCreateAuditEntries() throws Exception {
        // Login exitoso
        String validLogin = """
                {
                  "username": "admin",
                  "password": "admin123"
                }
                """;
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validLogin))
                .andExpect(status().isOk());

        // Login fallido
        String invalidLogin = """
                {
                  "username": "admin",
                  "password": "wrongpassword"
                }
                """;
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidLogin))
                .andExpect(status().isUnauthorized());

        // Consultar logs de auditoría como admin
        mockMvc.perform(get("/api/audit")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].action", hasItems(AuditAction.LOGIN_SUCCESS.name(), AuditAction.LOGIN_FAILED.name())));
    }

    @Test
    void technician_creation_shouldRecordAuditLog() throws Exception {
        String newTechPayload = """
                {
                  "username": "tech2",
                  "password": "tech2password",
                  "name": "Técnico Dos",
                  "email": "tech2@blue.local",
                  "department": "IT Operations"
                }
                """;

        mockMvc.perform(post("/api/users/technicians")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newTechPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/audit")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].action", hasItem(AuditAction.TECHNICIAN_CREATED.name())));
    }

    @Test
    void invalid_apiKey_shouldRecordInvalidApiKeyAuditLog() throws Exception {
        mockMvc.perform(post("/api/deployments")
                .header(API_KEY_HEADER, "invalid-bad-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deploymentId\":\"DEPLOY-AUDIT-01\",\"hostname\":\"PC-01\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/audit")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].action", hasItem(AuditAction.INVALID_API_KEY.name())));
    }

    @Test
    void audit_endpoint_shouldBeForbiddenForTechnician() throws Exception {
        mockMvc.perform(get("/api/audit")
                .header("Authorization", "Bearer " + techToken))
                .andExpect(status().isForbidden());
    }
}
