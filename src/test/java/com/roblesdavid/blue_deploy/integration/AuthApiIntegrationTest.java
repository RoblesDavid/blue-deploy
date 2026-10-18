package com.roblesdavid.blue_deploy.integration;

import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.UserRole;
import com.roblesdavid.blue_deploy.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthApiIntegrationTest extends BaseApiIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private AppUser createTestUser(String username, String rawPassword, UserRole role, boolean active) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setName(username.substring(0, 1).toUpperCase() + username.substring(1) + " User");
        user.setEmail(username + "@bluedeploy.local");
        user.setRole(role);
        user.setDepartment("Security Operations");
        user.setInitials(username.substring(0, Math.min(2, username.length())).toUpperCase());
        user.setActive(active);
        return appUserRepository.save(user);
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String payload = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("POST /api/auth/login - Éxito al iniciar sesión con credenciales válidas")
    void login_success() throws Exception {
        createTestUser("admin_user", "AdminPass123!", UserRole.ADMIN, true);

        String payload = """
                {
                  "username": "admin_user",
                  "password": "AdminPass123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin_user"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("admin_user@bluedeploy.local"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    @DisplayName("POST /api/auth/login - Falla con 401 si la contraseña es incorrecta")
    void login_badCredentials_invalidPassword() throws Exception {
        createTestUser("tech_user", "CorrectPassword123!", UserRole.TECHNICIAN, true);

        String payload = """
                {
                  "username": "tech_user",
                  "password": "WrongPassword!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Credenciales inválidas")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Falla con 401 si el usuario no existe")
    void login_badCredentials_userNotFound() throws Exception {
        String payload = """
                {
                  "username": "non_existing_user",
                  "password": "AnyPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Credenciales inválidas")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Falla con 401 si la cuenta está desactivada")
    void login_userDisabled() throws Exception {
        createTestUser("disabled_user", "Password123!", UserRole.TECHNICIAN, false);

        String payload = """
                {
                  "username": "disabled_user",
                  "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("desactivado")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Falla con 400 si los campos obligatorios están en blanco")
    void login_validationErrors() throws Exception {
        String payload = """
                {
                  "username": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Acceso sin token a endpoint protegido - Devuelve 401 Unauthorized")
    void accessProtectedEndpoint_withoutToken_unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/technicians"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Acceso de TECHNICIAN a gestión de técnicos - Devuelve 403 Forbidden")
    void accessTechnicianManagement_withTechnicianRole_forbidden() throws Exception {
        createTestUser("carlos", "TechPass123!", UserRole.TECHNICIAN, true);
        String token = loginAndGetToken("carlos", "TechPass123!");

        mockMvc.perform(get("/api/users/technicians")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        String createPayload = """
                {
                  "username": "pepe",
                  "password": "password123",
                  "name": "Pepe Garcia",
                  "email": "pepe@bluedeploy.local"
                }
                """;

        mockMvc.perform(post("/api/users/technicians")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Acceso de ADMIN a gestión de técnicos - Devuelve 200 OK y 201 Created")
    void accessTechnicianManagement_withAdminRole_success() throws Exception {
        createTestUser("admin_boss", "AdminPass123!", UserRole.ADMIN, true);
        String token = loginAndGetToken("admin_boss", "AdminPass123!");

        mockMvc.perform(get("/api/users/technicians")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String createPayload = """
                {
                  "username": "newtech",
                  "password": "TechPassword123!",
                  "name": "Nuevo Tecnico",
                  "email": "nuevo.tecnico@bluedeploy.local",
                  "department": "Soporte Nivel 2"
                }
                """;

        mockMvc.perform(post("/api/users/technicians")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newtech"))
                .andExpect(jsonPath("$.role").value("TECHNICIAN"));
    }

    @Test
    @DisplayName("Acceso de TECHNICIAN a su propio perfil /me - Devuelve 200 OK con sus datos")
    void accessProfile_withTechnicianRole_success() throws Exception {
        createTestUser("ana_tech", "AnaPass123!", UserRole.TECHNICIAN, true);
        String token = loginAndGetToken("ana_tech", "AnaPass123!");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana_tech"))
                .andExpect(jsonPath("$.email").value("ana_tech@bluedeploy.local"))
                .andExpect(jsonPath("$.role").value("TECHNICIAN"));

        String updatePayload = """
                {
                  "name": "Ana Maria Tech",
                  "email": "ana.maria@bluedeploy.local",
                  "department": "Operaciones TI"
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Maria Tech"))
                .andExpect(jsonPath("$.email").value("ana.maria@bluedeploy.local"));
    }
}
