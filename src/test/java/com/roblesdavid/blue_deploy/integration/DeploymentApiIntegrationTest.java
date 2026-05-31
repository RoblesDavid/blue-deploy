package com.roblesdavid.blue_deploy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeploymentApiIntegrationTest extends BaseApiIntegrationTest {

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
}
