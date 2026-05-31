package com.roblesdavid.blue_deploy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardApiIntegrationTest extends BaseApiIntegrationTest {

    @Test
    void getSummary_withoutData_shouldReturnZeros() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeployments").value(0))
                .andExpect(jsonPath("$.inProgress").value(0))
                .andExpect(jsonPath("$.domainJoined").value(0))
                .andExpect(jsonPath("$.postDomainPending").value(0))
                .andExpect(jsonPath("$.completed").value(0))
                .andExpect(jsonPath("$.completedWithWarnings").value(0))
                .andExpect(jsonPath("$.failed").value(0))
                .andExpect(jsonPath("$.nonCompliant").value(0))
                .andExpect(jsonPath("$.pendingCrowdStrike").value(0))
                .andExpect(jsonPath("$.pendingMicroClaudia").value(0));
    }

    @Test
    void getSummary_withMixedDeployments_shouldReturnAccurateCounters() throws Exception {
        createDeployment("DEPLOY-DASH-001").andExpect(status().isOk());

        createDeployment("DEPLOY-DASH-002").andExpect(status().isOk());
        postStep("DEPLOY-DASH-002", "DOMAIN_JOIN", "SUCCESS").andExpect(status().isOk());

        createDeployment("DEPLOY-DASH-003").andExpect(status().isOk());
        postStep("DEPLOY-DASH-003", "GPUPDATE", "SUCCESS").andExpect(status().isOk());

        createDeployment("DEPLOY-DASH-004").andExpect(status().isOk());
        postStep("DEPLOY-DASH-004", "FINAL_SECURITY_VALIDATION", "SUCCESS").andExpect(status().isOk());

        createDeployment("DEPLOY-DASH-005").andExpect(status().isOk());
        postStep("DEPLOY-DASH-005", "RADIUS_8021X_CONFIGURATION", "FAILED").andExpect(status().isOk());

        createDeployment("DEPLOY-DASH-006").andExpect(status().isOk());
        postCheck("DEPLOY-DASH-006", "LOCAL_ADMIN_VALIDATED", "FAILED", "HIGH").andExpect(status().isOk());

        createDeployment("DEPLOY-DASH-007").andExpect(status().isOk());
        postCheck("DEPLOY-DASH-007", "WSUS_CONFIGURED", "WARNING", "MEDIUM").andExpect(status().isOk());

        createDeployment("DEPLOY-DASH-008").andExpect(status().isOk());
        postCheck("DEPLOY-DASH-008", "CROWDSTRIKE_VALIDATED", "PENDING", "HIGH").andExpect(status().isOk());

        createDeployment("DEPLOY-DASH-009").andExpect(status().isOk());
        postCheck("DEPLOY-DASH-009", "MICROCLAUDIA_VALIDATED", "UNKNOWN", "HIGH").andExpect(status().isOk());

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeployments").value(9))
                .andExpect(jsonPath("$.inProgress").value(1))
                .andExpect(jsonPath("$.domainJoined").value(1))
                .andExpect(jsonPath("$.postDomainPending").value(3))
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.completedWithWarnings").value(1))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.nonCompliant").value(1))
                .andExpect(jsonPath("$.pendingCrowdStrike").value(1))
                .andExpect(jsonPath("$.pendingMicroClaudia").value(1));
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
                  "message": "dashboard step"
                }
                """.formatted(stepName, stepStatus);

        return mockMvc.perform(post("/api/deployments/{deploymentId}/steps", deploymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
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
                  "message": "dashboard check"
                }
                """.formatted(checkName, checkStatus, severity);

        return mockMvc.perform(post("/api/deployments/{deploymentId}/checks", deploymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }
}
