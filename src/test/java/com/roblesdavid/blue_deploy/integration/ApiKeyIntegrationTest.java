package com.roblesdavid.blue_deploy.integration;

import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiKeyIntegrationTest extends BaseApiIntegrationTest {

    private static final String VALID_DEPLOYMENT_PAYLOAD = """
            {
              "deploymentId": "DEPLOY-APIKEY-001",
              "hostname": "PC-APIKEY-001",
              "deviceModel": "Dell Latitude 5440",
              "domainName": "corp.local"
            }
            """;

    private static final String VALID_STEP_PAYLOAD = """
            {
              "stepName": "DOMAIN_JOIN",
              "stepStatus": "SUCCESS",
              "message": "Domain join complete"
            }
            """;

    private static final String VALID_CHECK_PAYLOAD = """
            {
              "checkName": "FIREWALL_ENABLED",
              "checkStatus": "PASSED",
              "severity": "HIGH",
              "message": "Firewall check passed"
            }
            """;

    @Test
    @DisplayName("POST /api/deployments - Petición sin API Key devuelve 401")
    void createDeployment_withoutApiKey_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_DEPLOYMENT_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/deployments - Petición con API Key incorrecta devuelve 401")
    void createDeployment_withInvalidApiKey_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/deployments")
                        .header(API_KEY_HEADER, "wrong-api-key-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_DEPLOYMENT_PAYLOAD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autorizado"))
                .andExpect(jsonPath("$.message").value("API Key no válida"));
    }

    @Test
    @DisplayName("POST /api/deployments - Petición con API Key correcta devuelve 200")
    void createDeployment_withValidApiKey_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/deployments")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_DEPLOYMENT_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value("DEPLOY-APIKEY-001"));
    }

    @Test
    @DisplayName("POST /api/deployments/{id}/steps - Petición sin API Key devuelve 401")
    void registerStep_withoutApiKey_shouldReturn401() throws Exception {
        createDeployment("DEPLOY-APIKEY-002").andExpect(status().isOk());

        mockMvc.perform(post("/api/deployments/DEPLOY-APIKEY-002/steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_STEP_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/deployments/{id}/steps - Petición con API Key incorrecta devuelve 401")
    void registerStep_withInvalidApiKey_shouldReturn401() throws Exception {
        createDeployment("DEPLOY-APIKEY-002").andExpect(status().isOk());

        mockMvc.perform(post("/api/deployments/DEPLOY-APIKEY-002/steps")
                        .header(API_KEY_HEADER, "wrong-api-key-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_STEP_PAYLOAD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autorizado"))
                .andExpect(jsonPath("$.message").value("API Key no válida"));
    }

    @Test
    @DisplayName("POST /api/deployments/{id}/steps - Petición con API Key correcta devuelve 200")
    void registerStep_withValidApiKey_shouldReturn200() throws Exception {
        createDeployment("DEPLOY-APIKEY-002").andExpect(status().isOk());

        mockMvc.perform(post("/api/deployments/DEPLOY-APIKEY-002/steps")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_STEP_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stepName").value("DOMAIN_JOIN"));
    }

    @Test
    @DisplayName("POST /api/deployments/{id}/checks - Petición sin API Key devuelve 401")
    void registerCheck_withoutApiKey_shouldReturn401() throws Exception {
        createDeployment("DEPLOY-APIKEY-003").andExpect(status().isOk());

        mockMvc.perform(post("/api/deployments/DEPLOY-APIKEY-003/checks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CHECK_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/deployments/{id}/checks - Petición con API Key incorrecta devuelve 401")
    void registerCheck_withInvalidApiKey_shouldReturn401() throws Exception {
        createDeployment("DEPLOY-APIKEY-003").andExpect(status().isOk());

        mockMvc.perform(post("/api/deployments/DEPLOY-APIKEY-003/checks")
                        .header(API_KEY_HEADER, "wrong-api-key-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CHECK_PAYLOAD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No autorizado"))
                .andExpect(jsonPath("$.message").value("API Key no válida"));
    }

    @Test
    @DisplayName("POST /api/deployments/{id}/checks - Petición con API Key correcta devuelve 200")
    void registerCheck_withValidApiKey_shouldReturn200() throws Exception {
        createDeployment("DEPLOY-APIKEY-003").andExpect(status().isOk());

        mockMvc.perform(post("/api/deployments/DEPLOY-APIKEY-003/checks")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CHECK_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkName").value("FIREWALL_ENABLED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/deployments - Administrador autenticado con JWT puede crear deployment sin API Key")
    void createDeployment_asAdminJwt_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_DEPLOYMENT_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value("DEPLOY-APIKEY-001"));
    }

    @Test
    @WithMockUser(username = "carlos", roles = {"TECHNICIAN"})
    @DisplayName("POST /api/deployments - Técnico autenticado sin API Key recibe 403 Forbidden")
    void createDeployment_asTechnicianWithoutApiKey_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_DEPLOYMENT_PAYLOAD))
                .andExpect(status().isForbidden());
    }
}
