package com.roblesdavid.blue_deploy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityCheckApiIntegrationTest extends BaseApiIntegrationTest {

    @Test
    void registerPendingCheck_shouldSetPostDomainPendingAndPendingCompliance() throws Exception {
        String deploymentId = "DEPLOY-PC300-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        postCheck(deploymentId, "CROWDSTRIKE_VALIDATED", "PENDING", "HIGH")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkStatus").value("PENDING"));

        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("POST_DOMAIN_PENDING"))
                .andExpect(jsonPath("$.complianceStatus").value("PENDING"));
    }

    @Test
    void registerFailedCheck_shouldSetNonCompliant() throws Exception {
        String deploymentId = "DEPLOY-PC301-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        postCheck(deploymentId, "MICROCLAUDIA_VALIDATED", "FAILED", "CRITICAL")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("NON_COMPLIANT"))
                .andExpect(jsonPath("$.complianceStatus").value("NON_COMPLIANT"));
    }

    @Test
    void registerWarningCheck_shouldSetCompletedWithWarnings() throws Exception {
        String deploymentId = "DEPLOY-PC302-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        postCheck(deploymentId, "WSUS_CONFIGURATION", "WARNING", "MEDIUM")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("COMPLETED_WITH_WARNINGS"))
                .andExpect(jsonPath("$.complianceStatus").value("NON_COMPLIANT"))
                .andExpect(jsonPath("$.finishedAt").isNotEmpty());
    }

    @Test
    void registerPassedCheck_shouldSetCompletedAndCompliant() throws Exception {
        String deploymentId = "DEPLOY-PC303-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        postCheck(deploymentId, "FIREWALL_ENABLED", "PASSED", "HIGH")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.complianceStatus").value("COMPLIANT"));
    }

    @Test
    void registerCheck_whenDeploymentIsFailed_shouldKeepFailedStatus() throws Exception {
        String deploymentId = "DEPLOY-PC304-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        postStep(deploymentId, "RADIUS_8021X_CONFIGURATION", "FAILED")
                .andExpect(status().isOk());

        postCheck(deploymentId, "FIREWALL_ENABLED", "PASSED", "HIGH")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentStatus").value("FAILED"))
                .andExpect(jsonPath("$.complianceStatus").value("COMPLIANT"));
    }

    @Test
    void registerCheck_withInvalidEnum_shouldReturnBadRequest() throws Exception {
        String deploymentId = "DEPLOY-PC305-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        String invalidPayload = """
                {
                  "checkName": "FIREWALL_ENABLED",
                  "checkStatus": "PASSED",
                  "severity": "VERY_HIGH",
                  "message": "invalid enum"
                }
                """;

        mockMvc.perform(post("/api/deployments/{deploymentId}/checks", deploymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerCheck_withSqlInjectionLikePayload_shouldStoreLiteralData() throws Exception {
        String deploymentId = "DEPLOY-PC306-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        String checkName = "CROWDSTRIKE_VALIDATED'; DROP TABLE security_check; --";
        postCheck(deploymentId, checkName, "PENDING", "HIGH")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkName", containsString("DROP TABLE")));

        mockMvc.perform(get("/api/deployments/{deploymentId}/checks", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].checkName", containsString("DROP TABLE")));
    }

    private org.springframework.test.web.servlet.ResultActions postCheck(
            String deploymentId,
            String checkName,
            String checkStatus,
            String severity
    ) throws Exception {
        String payload = """
                {
                  "checkName": "%s",
                  "checkStatus": "%s",
                  "severity": "%s",
                  "expectedValue": "Expected",
                  "actualValue": "Actual",
                  "message": "security check"
                }
                """.formatted(checkName, checkStatus, severity);

        return mockMvc.perform(post("/api/deployments/{deploymentId}/checks", deploymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
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
                  "message": "step %s status %s"
                }
                """.formatted(stepName, stepStatus, stepName, stepStatus);

        return mockMvc.perform(post("/api/deployments/{deploymentId}/steps", deploymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }
}
