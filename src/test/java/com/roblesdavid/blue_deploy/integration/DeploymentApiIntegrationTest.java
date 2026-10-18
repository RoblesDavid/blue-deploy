package com.roblesdavid.blue_deploy.integration;

import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeploymentApiIntegrationTest extends BaseApiIntegrationTest {

    private AppUser createTechnician(String username, String name) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword("password123");
        user.setName(name);
        user.setEmail(username + "@bluedeploy.local");
        user.setRole(UserRole.TECHNICIAN);
        user.setDepartment("Soporte IT");
        user.setActive(true);
        return appUserRepository.save(user);
    }

    private void createDeploymentWithTech(String deploymentId, Long technicianId) throws Exception {
        String payload = """
                {
                  "deploymentId": "%s",
                  "hostname": "PC-%s",
                  "deviceModel": "Dell Latitude 5440",
                  "domainName": "corp.local",
                  "technicianId": %d
                }
                """.formatted(deploymentId, deploymentId, technicianId);

        mockMvc.perform(post("/api/deployments")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    void createDeployment_shouldSetInitialStatuses() throws Exception {
        createDeployment("DEPLOY-PC100-20260516")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value("DEPLOY-PC100-20260516"))
                .andExpect(jsonPath("$.deploymentStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.complianceStatus").value("PENDING"))
                .andExpect(jsonPath("$.currentStep").value("DEPLOYMENT_CREATED"));
    }

    @Test
    @DisplayName("POST /api/deployments - Crear deployment con técnico asignado")
    void createDeployment_withAssignedTechnician_shouldReturnTechnicianDetails() throws Exception {
        AppUser tech = createTechnician("carlos", "Carlos Rodríguez");

        String payload = """
                {
                  "deploymentId": "DEPLOY-TECH-01",
                  "hostname": "PC-CARLOS",
                  "deviceModel": "Dell Latitude",
                  "domainName": "corp.local",
                  "technicianId": %d
                }
                """.formatted(tech.getId());

        mockMvc.perform(post("/api/deployments")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value("DEPLOY-TECH-01"))
                .andExpect(jsonPath("$.technicianId").value(tech.getId()))
                .andExpect(jsonPath("$.technicianName").value("Carlos Rodríguez"));
    }

    @Test
    void createDeployment_withDuplicateId_shouldReturnServerError() throws Exception {
        createDeployment("DEPLOY-PC101-20260516").andExpect(status().isOk());

        createDeployment("DEPLOY-PC101-20260516")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Ya existe un deployment")));
    }

    @Test
    void createDeployment_withInvalidPayload_shouldReturnBadRequest() throws Exception {
        String invalidPayload = """
                {
                  "deploymentId": "",
                  "hostname": "",
                  "deviceModel": "Dell",
                  "domainName": "corp.local"
                }
                """;

        mockMvc.perform(post("/api/deployments")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDeployment_withSqlInjectionLikeId_shouldNotRetrieveOtherDeployments() throws Exception {
        createDeployment("DEPLOY-PC102-20260516").andExpect(status().isOk());

        String maliciousId = "DEPLOY-PC102-20260516' OR '1'='1";

        mockMvc.perform(get("/api/deployments/{deploymentId}", maliciousId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/deployments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deploymentId").value("DEPLOY-PC102-20260516"));
    }

    @Test
    void getAllDeployments_shouldReturnPersistedItems() throws Exception {
        createDeployment("DEPLOY-PC103-20260516").andExpect(status().isOk());
        createDeployment("DEPLOY-PC104-20260516").andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("GET /api/deployments - Admin obtiene todos los deployments")
    void getDeployments_asAdmin_shouldReturnAllDeployments() throws Exception {
        AppUser tech1 = createTechnician("carlos", "Carlos Rodríguez");
        AppUser tech2 = createTechnician("laura", "Laura Gómez");

        createDeploymentWithTech("DEPLOY-101", tech1.getId());
        createDeploymentWithTech("DEPLOY-102", tech2.getId());

        mockMvc.perform(get("/api/deployments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "carlos", roles = {"TECHNICIAN"})
    @DisplayName("GET /api/deployments - Técnico obtiene solo sus deployments asignados")
    void getDeployments_asTechnician_shouldReturnOnlyOwnDeployments() throws Exception {
        AppUser tech1 = createTechnician("carlos", "Carlos Rodríguez");
        AppUser tech2 = createTechnician("laura", "Laura Gómez");

        createDeploymentWithTech("DEPLOY-201", tech1.getId());
        createDeploymentWithTech("DEPLOY-202", tech2.getId());

        mockMvc.perform(get("/api/deployments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].deploymentId").value("DEPLOY-201"))
                .andExpect(jsonPath("$[0].technicianName").value("Carlos Rodríguez"));
    }

    @Test
    @WithMockUser(username = "laura", roles = {"TECHNICIAN"})
    @DisplayName("GET /api/deployments/my - Técnico obtiene sus deployments mediante /my")
    void getMyDeployments_asTechnician_shouldReturnOwnDeployments() throws Exception {
        AppUser tech1 = createTechnician("carlos", "Carlos Rodríguez");
        AppUser tech2 = createTechnician("laura", "Laura Gómez");

        createDeploymentWithTech("DEPLOY-301", tech1.getId());
        createDeploymentWithTech("DEPLOY-302", tech2.getId());

        mockMvc.perform(get("/api/deployments/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].deploymentId").value("DEPLOY-302"))
                .andExpect(jsonPath("$[0].technicianName").value("Laura Gómez"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("PUT /api/deployments/{id}/assign-technician/{id} - Admin asigna deployment a técnico")
    void assignTechnician_asAdmin_shouldUpdateTechnician() throws Exception {
        AppUser tech1 = createTechnician("carlos", "Carlos Rodríguez");
        AppUser tech2 = createTechnician("laura", "Laura Gómez");

        createDeploymentWithTech("DEPLOY-401", tech1.getId());

        mockMvc.perform(put("/api/deployments/{deploymentId}/assign-technician/{technicianId}", "DEPLOY-401", tech2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value("DEPLOY-401"))
                .andExpect(jsonPath("$.technicianId").value(tech2.getId()))
                .andExpect(jsonPath("$.technicianName").value("Laura Gómez"));
    }

    @Test
    @WithMockUser(username = "carlos", roles = {"TECHNICIAN"})
    @DisplayName("PUT /api/deployments/{id}/assign-technician/{id} - Técnico no puede reasignar deployments (403)")
    void assignTechnician_asTechnician_shouldReturnForbidden() throws Exception {
        AppUser tech1 = createTechnician("carlos", "Carlos Rodríguez");
        AppUser tech2 = createTechnician("laura", "Laura Gómez");

        createDeploymentWithTech("DEPLOY-501", tech1.getId());

        mockMvc.perform(put("/api/deployments/{deploymentId}/assign-technician/{technicianId}", "DEPLOY-501", tech2.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Acceso denegado"));
    }

    @Test
    @DisplayName("PUT /api/deployments/{id}/assign-technician/{id} - Usuario no autenticado recibe 401")
    void assignTechnician_unauthenticated_shouldReturnUnauthorized() throws Exception {
        AppUser tech1 = createTechnician("carlos", "Carlos Rodríguez");
        createDeploymentWithTech("DEPLOY-601", tech1.getId());

        mockMvc.perform(put("/api/deployments/{deploymentId}/assign-technician/{technicianId}", "DEPLOY-601", tech1.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/deployments/{deploymentId} - Devuelve steps y checks incluidos en el detalle")
    void getDeploymentById_shouldIncludeStepsAndChecks() throws Exception {
        createDeployment("DEPLOY-DETAIL-001").andExpect(status().isOk());

        String stepPayload = """
                {
                  "stepName": "WINDOWS_INSTALLATION",
                  "stepStatus": "SUCCESS",
                  "message": "Instalación completada"
                }
                """;
        mockMvc.perform(post("/api/deployments/DEPLOY-DETAIL-001/steps")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stepPayload))
                .andExpect(status().isOk());

        String checkPayload = """
                {
                  "checkName": "FIREWALL_ENABLED",
                  "checkStatus": "PASSED",
                  "severity": "HIGH",
                  "expectedValue": "Enabled",
                  "actualValue": "Enabled",
                  "message": "Firewall OK"
                }
                """;
        mockMvc.perform(post("/api/deployments/DEPLOY-DETAIL-001/checks")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkPayload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments/DEPLOY-DETAIL-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value("DEPLOY-DETAIL-001"))
                .andExpect(jsonPath("$.steps.length()").value(1))
                .andExpect(jsonPath("$.steps[0].stepName").value("WINDOWS_INSTALLATION"))
                .andExpect(jsonPath("$.checks.length()").value(1))
                .andExpect(jsonPath("$.checks[0].checkName").value("FIREWALL_ENABLED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("PUT /api/deployments/{id}/status - Modificación manual de estado por Admin")
    void updateDeploymentStatus_asAdmin_shouldUpdateStatusAndCompliance() throws Exception {
        createDeployment("DEPLOY-MANUAL-001").andExpect(status().isOk());

        String updatePayload = """
                {
                  "deploymentStatus": "COMPLETED",
                  "complianceStatus": "COMPLIANT",
                  "currentStep": "MANUAL_VALIDATION_DONE",
                  "note": "Aprobado manualmente por SecOps"
                }
                """;

        mockMvc.perform(put("/api/deployments/DEPLOY-MANUAL-001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value("DEPLOY-MANUAL-001"))
                .andExpect(jsonPath("$.deploymentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.complianceStatus").value("COMPLIANT"))
                .andExpect(jsonPath("$.currentStep").value("MANUAL_VALIDATION_DONE"));
    }

    @Test
    @WithMockUser(username = "carlos", roles = {"TECHNICIAN"})
    @DisplayName("PUT /api/deployments/{id}/status - Técnico actualiza su propio despliegue")
    void updateDeploymentStatus_asAssignedTechnician_shouldSucceed() throws Exception {
        AppUser tech = createTechnician("carlos", "Carlos Rodríguez");
        createDeploymentWithTech("DEPLOY-MANUAL-TECH-01", tech.getId());

        String updatePayload = """
                {
                  "deploymentStatus": "COMPLETED_WITH_WARNINGS",
                  "complianceStatus": "NON_COMPLIANT",
                  "currentStep": "AD_WSUS_TASK_REGISTERED",
                  "note": "Revisión técnica en progreso"
                }
                """;

        mockMvc.perform(put("/api/deployments/DEPLOY-MANUAL-TECH-01/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value("DEPLOY-MANUAL-TECH-01"))
                .andExpect(jsonPath("$.deploymentStatus").value("COMPLETED_WITH_WARNINGS"))
                .andExpect(jsonPath("$.complianceStatus").value("NON_COMPLIANT"));
    }

    @Test
    @WithMockUser(username = "ana", roles = {"TECHNICIAN"})
    @DisplayName("PUT /api/deployments/{id}/status - Técnico no puede modificar despliegue de otro técnico (403)")
    void updateDeploymentStatus_asOtherTechnician_shouldReturnForbidden() throws Exception {
        AppUser carlos = createTechnician("carlos", "Carlos Rodríguez");
        createDeploymentWithTech("DEPLOY-MANUAL-CARLOS-01", carlos.getId());

        String updatePayload = """
                {
                  "deploymentStatus": "COMPLETED"
                }
                """;

        mockMvc.perform(put("/api/deployments/DEPLOY-MANUAL-CARLOS-01/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isForbidden());
    }
}
