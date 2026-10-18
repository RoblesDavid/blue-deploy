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
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerCheck_withAliasFields_shouldSucceed() throws Exception {
        String deploymentId = "DEPLOY-PC305B-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        String aliasPayload = """
                {
                  "checkType": "CROWDSTRIKE_VALIDATED",
                  "status": "PASSED",
                  "severity": "CRITICAL",
                  "expectedValue": "Running",
                  "actualValue": "Running",
                  "message": "Sensor Falcon en ejecución y sincronizado"
                }
                """;

        mockMvc.perform(post("/api/deployments/{deploymentId}/checks", deploymentId)
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aliasPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkName").value("CROWDSTRIKE_VALIDATED"))
                .andExpect(jsonPath("$.checkStatus").value("PASSED"));
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

    @Test
    void resolveCheck_shouldUpdateCheckToPassedAndRecalculateCompliance() throws Exception {
        String deploymentId = "DEPLOY-PC307-20260516";
        createDeployment(deploymentId).andExpect(status().isOk());

        // First register a failed check
        postCheck(deploymentId, "CROWDSTRIKE_VALIDATED", "FAILED", "CRITICAL")
                .andExpect(status().isOk());

        // Get the check ID
        String checksJson = mockMvc.perform(get("/api/deployments/{deploymentId}/checks", deploymentId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract check ID using JsonPath
        Integer checkId = com.jayway.jsonpath.JsonPath.read(checksJson, "$[0].id");

        // Resolve the check as technician/admin
        String resolvePayload = """
                {
                  "message": "Sensor Falcon instalado y validado manualmente"
                }
                """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/deployments/{deploymentId}/checks/{checkId}/resolve", deploymentId, checkId)
                        .header("Authorization", "Bearer " + obtainAdminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolvePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkStatus").value("PASSED"))
                .andExpect(jsonPath("$.message").value("Sensor Falcon instalado y validado manualmente"));

        // Verify deployment is now COMPLIANT
        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complianceStatus").value("COMPLIANT"))
                .andExpect(jsonPath("$.deploymentStatus").value("COMPLETED"));
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
                .header(API_KEY_HEADER, TEST_API_KEY)
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
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }
}
