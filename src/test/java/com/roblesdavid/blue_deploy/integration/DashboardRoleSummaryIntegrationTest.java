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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardRoleSummaryIntegrationTest extends BaseApiIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String techTokenCarlos;
    private AppUser techCarlos;
    private AppUser techAna;

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

        techCarlos = new AppUser();
        techCarlos.setUsername("carlos");
        techCarlos.setPassword(passwordEncoder.encode("carlos123"));
        techCarlos.setName("Carlos Técnico");
        techCarlos.setEmail("carlos@blue.local");
        techCarlos.setRole(UserRole.TECHNICIAN);
        techCarlos.setActive(true);
        appUserRepository.save(techCarlos);

        techAna = new AppUser();
        techAna.setUsername("ana");
        techAna.setPassword(passwordEncoder.encode("ana123"));
        techAna.setName("Ana Técnico");
        techAna.setEmail("ana@blue.local");
        techAna.setRole(UserRole.TECHNICIAN);
        techAna.setActive(true);
        appUserRepository.save(techAna);

        adminToken = jwtService.generateToken(Map.of("role", "ADMIN"), "admin");
        techTokenCarlos = jwtService.generateToken(Map.of("role", "TECHNICIAN"), "carlos");
    }

    @Test
    void getSummary_adminGetsGlobal_technicianGetsScopedMetrics() throws Exception {
        // Despliegue 1 para Carlos (COMPLETED)
        String depCarlos = """
                {
                  "deploymentId": "DEP-CARLOS-01",
                  "hostname": "PC-CARLOS",
                  "technicianId": %d
                }
                """.formatted(techCarlos.getId());
        mockMvc.perform(post("/api/deployments")
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(depCarlos))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/deployments/{id}/steps", "DEP-CARLOS-01")
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stepName\":\"FINAL_SECURITY_VALIDATION\",\"stepStatus\":\"SUCCESS\"}"))
                .andExpect(status().isOk());

        // Despliegue 2 para Ana (FAILED)
        String depAna = """
                {
                  "deploymentId": "DEP-ANA-01",
                  "hostname": "PC-ANA",
                  "technicianId": %d
                }
                """.formatted(techAna.getId());
        mockMvc.perform(post("/api/deployments")
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(depAna))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/deployments/{id}/steps", "DEP-ANA-01")
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stepName\":\"DOMAIN_JOIN\",\"stepStatus\":\"FAILED\",\"errorCode\":\"0x800\"}"))
                .andExpect(status().isOk());

        // Admin ve total 2, completados 1, fallidos 1
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeployments").value(2))
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.assignedTechnicians").value(2));

        // Carlos ve solo su despliegue: total 1, completados 1, fallidos 0
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + techTokenCarlos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeployments").value(1))
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.failed").value(0));
    }
}
