package com.roblesdavid.blue_deploy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roblesdavid.blue_deploy.repository.AlertRepository;
import com.roblesdavid.blue_deploy.repository.AppUserRepository;
import com.roblesdavid.blue_deploy.repository.CheckRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentStepRepository;
import com.roblesdavid.blue_deploy.repository.SecurityCheckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class BaseApiIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private CheckRepository checkRepository;

    @Autowired
    private SecurityCheckRepository securityCheckRepository;

    @Autowired
    private DeploymentStepRepository deploymentStepRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    @Autowired
    protected AppUserRepository appUserRepository;

    @BeforeEach
    void cleanupDatabase() {
        alertRepository.deleteAll();
        checkRepository.deleteAll();
        securityCheckRepository.deleteAll();
        deploymentStepRepository.deleteAll();
        deploymentRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    protected ResultActions createDeployment(String deploymentId) throws Exception {
        String payload = """
                {
                  "deploymentId": "%s",
                  "hostname": "PC-TEST",
                  "deviceModel": "Dell Latitude 5440",
                  "domainName": "corp.local"
                }
                """.formatted(deploymentId);

        return mockMvc.perform(post("/api/deployments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }
}
