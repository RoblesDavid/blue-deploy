package com.roblesdavid.blue_deploy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeploymentStepApiIntegrationTest extends BaseApiIntegrationTest {

    @Test
    void registerStep_shouldUpdateDeploymentStatusLifecycle() throws Exception {
        String deploymentId = "DEPLOY-PC200-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        postStep(deploymentId, "DOMAIN_JOIN", "SUCCESS")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stepName").value("DOMAIN_JOIN"));

        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("DOMAIN_JOINED"));

        postStep(deploymentId, "GPUPDATE", "SUCCESS")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("POST_DOMAIN_PENDING"));

        postStep(deploymentId, "FINAL_SECURITY_VALIDATION", "SUCCESS")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.finishedAt").isNotEmpty());
    }

    @Test
    void registerStep_withFailedStatus_shouldMarkDeploymentFailed() throws Exception {
        String deploymentId = "DEPLOY-PC201-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        postStep(deploymentId, "RADIUS_8021X_CONFIGURATION", "FAILED")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("FAILED"))
                .andExpect(jsonPath("$.currentStep").value("RADIUS_8021X_CONFIGURATION"))
                .andExpect(jsonPath("$.finishedAt").isNotEmpty());
    }

    @Test
    void registerStep_withInvalidPayload_shouldReturnBadRequest() throws Exception {
        String deploymentId = "DEPLOY-PC202-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        String invalidPayload = """
                {
                  "stepName": "",
                  "message": "fase sin estado"
                }
                """;

        mockMvc.perform(post("/api/deployments/{deploymentId}/steps", deploymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSteps_shouldReturnChronologicalList() throws Exception {
        String deploymentId = "DEPLOY-PC203-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        postStep(deploymentId, "WINDOWS_INSTALLATION", "SUCCESS").andExpect(status().isOk());
        postStep(deploymentId, "DRIVER_INSTALLATION", "SUCCESS").andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments/{deploymentId}/steps", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].stepName").value("WINDOWS_INSTALLATION"))
                .andExpect(jsonPath("$[1].stepName").value("DRIVER_INSTALLATION"));
    }

    private org.springframework.test.web.servlet.ResultActions postStep(
            String deploymentId,
            String stepName,
            String stepStatus
    ) throws Exception {
        String payload = """
                {
                  "stepName": "%s",
                  "stepStatus": "%s",
                  "message": "step %s status %s",
                  "rawOutput": "ok"
                }
                """.formatted(stepName, stepStatus, stepName, stepStatus);

        return mockMvc.perform(post("/api/deployments/{deploymentId}/steps", deploymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }
}
