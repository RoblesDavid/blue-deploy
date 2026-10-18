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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DeploymentReportIntegrationTest extends BaseApiIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String techTokenCarlos;
    private String techTokenAna;
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
        techTokenAna = jwtService.generateToken(Map.of("role", "TECHNICIAN"), "ana");
    }

    @Test
    void downloadReport_adminCanDownloadAnyReport() throws Exception {
        String deploymentId = "DEPLOY-PDF-001";
        createDeployment(deploymentId).andExpect(status().isOk());

        // Añadir step y check
        mockMvc.perform(post("/api/deployments/{deploymentId}/steps", deploymentId)
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stepName\":\"DOMAIN_JOIN\",\"stepStatus\":\"SUCCESS\",\"message\":\"Unido a dominio\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/deployments/{deploymentId}/checks", deploymentId)
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"checkName\":\"CROWDSTRIKE_VALIDATED\",\"checkStatus\":\"PASSED\",\"severity\":\"HIGH\"}"))
                .andExpect(status().isOk());

        byte[] content = mockMvc.perform(get("/api/deployments/{deploymentId}/report.pdf", deploymentId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();

        assertTrue(content.length > 500, "El PDF generado debe contener bytes válidos");
        // Comprobar cabecera mágica de PDF (%PDF)
        String pdfHeader = new String(content, 0, 4);
        assertTrue(pdfHeader.startsWith("%PDF"), "El archivo debe iniciar con la cabecera estándar %PDF");
    }

    @Test
    void downloadReport_technicianCanDownloadOwnReport_butForbiddenForOthers() throws Exception {
        String depCarlos = "DEPLOY-PDF-CARLOS";

        // Crear despliegue asignado a Carlos
        String payload = """
                {
                  "deploymentId": "%s",
                  "hostname": "PC-CARLOS",
                  "deviceModel": "Lenovo T14",
                  "technicianId": %d
                }
                """.formatted(depCarlos, techCarlos.getId());

        mockMvc.perform(post("/api/deployments")
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        // Carlos puede descargarlo
        mockMvc.perform(get("/api/deployments/{deploymentId}/report.pdf", depCarlos)
                .header("Authorization", "Bearer " + techTokenCarlos))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        // Ana no tiene acceso al despliegue de Carlos
        mockMvc.perform(get("/api/deployments/{deploymentId}/report.pdf", depCarlos)
                .header("Authorization", "Bearer " + techTokenAna))
                .andExpect(status().isForbidden());
    }
}
