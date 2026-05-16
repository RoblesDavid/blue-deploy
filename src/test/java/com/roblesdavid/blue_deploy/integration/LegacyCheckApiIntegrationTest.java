package com.roblesdavid.blue_deploy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LegacyCheckApiIntegrationTest extends BaseApiIntegrationTest {

    @Test
    void healthEndpoint_shouldReturnApiOk() throws Exception {
        mockMvc.perform(get("/api/checks/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("API OK"));
    }

    @Test
    void login_withValidCredentials_shouldReturnTokenOk() throws Exception {
        String payload = """
                {
                  "user": "admin",
                  "password": "admin"
                }
                """;

        mockMvc.perform(post("/api/checks/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("TOKEN_OK"));
    }

    @Test
    void login_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        String payload = """
                {
                  "user": "admin",
                  "password": "wrong"
                }
                """;

        mockMvc.perform(post("/api/checks/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void receiveCheck_withValidPayload_shouldPersistAndReturnSuccessMessage() throws Exception {
        String payload = """
                {
                  "date": "2026-05-16T09:00:00Z",
                  "user": "tecnico",
                  "hostname": "PC400",
                  "checks": {
                    "firewall_enabled": true,
                    "antivirus_enabled": true,
                    "disk_encrypted": true,
                    "updates_enabled": true
                  }
                }
                """;

        mockMvc.perform(post("/api/checks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("Guardado correctamente"));

        mockMvc.perform(get("/api/checks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].hostname").value("PC400"));
    }

    @Test
    void receiveCheck_withInvalidPayload_shouldReturnBadRequest() throws Exception {
        String payload = """
                {
                  "date": "2026-05-16T09:00:00Z",
                  "user": "",
                  "hostname": "",
                  "checks": null
                }
                """;

        mockMvc.perform(post("/api/checks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void receiveCheck_withSqlInjectionLikeHostname_shouldStoreLiteralValue() throws Exception {
        String payload = """
                {
                  "date": "2026-05-16T09:00:00Z",
                  "user": "tecnico",
                  "hostname": "PC401'; DROP TABLE system_check; --",
                  "checks": {
                    "firewall_enabled": false,
                    "antivirus_enabled": true,
                    "disk_encrypted": false,
                    "updates_enabled": true
                  }
                }
                """;

        mockMvc.perform(post("/api/checks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/checks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname", containsString("DROP TABLE")));
    }
}
