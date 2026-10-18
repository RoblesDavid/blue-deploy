package com.roblesdavid.blue_deploy.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RealDeploymentExamplesIntegrationTest extends BaseApiIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void replayAllRealDeploymentExamples_shouldIngestCleanlyAndSanitized() throws Exception {
        String deploymentId = "DEPLOY-AGSNEG-TESTICBLUE-20261002";

        // 1. Crear despliegue inicial
        String createPayload = """
                {
                  "deploymentId": "%s",
                  "hostname": "TESTICBLUE",
                  "deviceModel": "HP ProDesk 400 G5 SFF",
                  "domainName": "dominio.local"
                }
                """.formatted(deploymentId);

        mockMvc.perform(post("/api/deployments")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value(deploymentId))
                .andExpect(jsonPath("$.domainName").value("dominio.local"));

        // 2. Cargar y enviar todos los archivos JSON de examples
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("file:scripts/powershell/bluedeploy/examples/*.json");

        Arrays.sort(resources, Comparator.comparing(Resource::getFilename));

        for (Resource resource : resources) {
            try (InputStream is = resource.getInputStream()) {
                JsonNode rootNode = objectMapper.readTree(is);
                JsonNode payloadNode = rootNode.path("payload");
                String apiPath = rootNode.path("apiPath").asText("/api/deployments/" + deploymentId + "/steps");

                mockMvc.perform(post(apiPath)
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payloadNode)))
                        .andExpect(status().isOk());
            }
        }

        // 3. Registrar los security checks
        String[] checks = {
                "FIREWALL_ENABLED", "WSUS_CONFIGURED", "CROWDSTRIKE_VALIDATED", "MICROCLAUDIA_VALIDATED", "LOCAL_ADMIN_VALIDATED"
        };
        for (String checkName : checks) {
            String checkPayload = """
                    {
                      "checkName": "%s",
                      "checkStatus": "PASSED",
                      "severity": "CRITICAL",
                      "expectedValue": "Running",
                      "actualValue": "Running",
                      "message": "Comprobacion validada en entorno real"
                    }
                    """.formatted(checkName);

            mockMvc.perform(post("/api/deployments/{deploymentId}/checks", deploymentId)
                            .header(API_KEY_HEADER, TEST_API_KEY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(checkPayload))
                    .andExpect(status().isOk());
        }

        // 4. Validar estado general del despliegue y sanitización
        mockMvc.perform(get("/api/deployments/{deploymentId}", deploymentId)
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deploymentId").value(deploymentId))
                .andExpect(jsonPath("$.domainName").value("dominio.local"))
                .andExpect(jsonPath("$.deploymentStatus").value(anyOf(is("COMPLETED"), is("COMPLETED_WITH_WARNINGS"))))
                .andExpect(jsonPath("$.complianceStatus").value("COMPLIANT"));

        // 5. Validar lista de pasos y contenido sanitizado
        mockMvc.perform(get("/api/deployments/{deploymentId}/steps", deploymentId)
                        .header(API_KEY_HEADER, TEST_API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(18)))
                .andExpect(jsonPath("$[*].rawOutput", not(hasItem(containsString("junta-andalucia")))))
                .andExpect(jsonPath("$[*].rawOutput", not(hasItem(containsString("Administrador")))));
    }
}
