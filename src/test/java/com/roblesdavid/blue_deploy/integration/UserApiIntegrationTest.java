package com.roblesdavid.blue_deploy.integration;

import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserApiIntegrationTest extends BaseApiIntegrationTest {

    @Test
    @DisplayName("POST /api/users/technicians - Crea un técnico con éxito")
    void createTechnician_success() throws Exception {
        String payload = """
                {
                  "username": "carlos",
                  "password": "password123",
                  "name": "Carlos Rodríguez",
                  "email": "carlos.rodriguez@bluedeploy.local",
                  "department": "Blue Team - Infraestructura"
                }
                """;

        mockMvc.perform(post("/api/users/technicians")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("carlos"))
                .andExpect(jsonPath("$.name").value("Carlos Rodríguez"))
                .andExpect(jsonPath("$.initials").value("CR"))
                .andExpect(jsonPath("$.email").value("carlos.rodriguez@bluedeploy.local"))
                .andExpect(jsonPath("$.role").value("TECHNICIAN"))
                .andExpect(jsonPath("$.department").value("Blue Team - Infraestructura"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/users/technicians - Falla si el username ya existe")
    void createTechnician_duplicateUsername() throws Exception {
        AppUser existing = new AppUser();
        existing.setUsername("carlos");
        existing.setPassword("pass123");
        existing.setName("Carlos R");
        existing.setEmail("carlos1@bluedeploy.local");
        existing.setRole(UserRole.TECHNICIAN);
        appUserRepository.save(existing);

        String payload = """
                {
                  "username": "carlos",
                  "password": "password123",
                  "name": "Carlos Otro",
                  "email": "otro@bluedeploy.local",
                  "department": "Blue Team"
                }
                """;

        mockMvc.perform(post("/api/users/technicians")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nombre de usuario ya está en uso")));
    }

    @Test
    @DisplayName("POST /api/users/technicians - Falla si el email ya existe")
    void createTechnician_duplicateEmail() throws Exception {
        AppUser existing = new AppUser();
        existing.setUsername("carlos1");
        existing.setPassword("pass123");
        existing.setName("Carlos R");
        existing.setEmail("carlos@bluedeploy.local");
        existing.setRole(UserRole.TECHNICIAN);
        appUserRepository.save(existing);

        String payload = """
                {
                  "username": "carlos2",
                  "password": "password123",
                  "name": "Carlos Otro",
                  "email": "carlos@bluedeploy.local",
                  "department": "Blue Team"
                }
                """;

        mockMvc.perform(post("/api/users/technicians")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("correo electrónico ya está en uso")));
    }

    @Test
    @DisplayName("GET /api/users/technicians - Devuelve únicamente técnicos")
    void getTechnicians_returnsOnlyTechnicians() throws Exception {
        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setPassword("adminpass");
        admin.setName("Admin Principal");
        admin.setEmail("admin@bluedeploy.local");
        admin.setRole(UserRole.ADMIN);
        appUserRepository.save(admin);

        AppUser tech1 = new AppUser();
        tech1.setUsername("ana");
        tech1.setPassword("techpass");
        tech1.setName("Ana García");
        tech1.setEmail("ana@bluedeploy.local");
        tech1.setRole(UserRole.TECHNICIAN);
        appUserRepository.save(tech1);

        mockMvc.perform(get("/api/users/technicians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("ana"))
                .andExpect(jsonPath("$[0].role").value("TECHNICIAN"));
    }

    @Test
    @DisplayName("PUT /api/users/technicians/{id} - Modifica un técnico")
    void updateTechnician_success() throws Exception {
        AppUser tech = new AppUser();
        tech.setUsername("pedro");
        tech.setPassword("techpass");
        tech.setName("Pedro Sánchez");
        tech.setEmail("pedro@bluedeploy.local");
        tech.setRole(UserRole.TECHNICIAN);
        tech.setDepartment("Helpdesk");
        AppUser saved = appUserRepository.save(tech);

        String payload = """
                {
                  "name": "Pedro Sánchez Actualizado",
                  "email": "pedro.nuevo@bluedeploy.local",
                  "department": "Operaciones SOC"
                }
                """;

        mockMvc.perform(put("/api/users/technicians/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pedro Sánchez Actualizado"))
                .andExpect(jsonPath("$.email").value("pedro.nuevo@bluedeploy.local"))
                .andExpect(jsonPath("$.department").value("Operaciones SOC"));
    }

    @Test
    @DisplayName("PATCH /api/users/technicians/{id}/disable - Desactiva un técnico")
    void disableTechnician_success() throws Exception {
        AppUser tech = new AppUser();
        tech.setUsername("laura");
        tech.setPassword("techpass");
        tech.setName("Laura Gómez");
        tech.setEmail("laura@bluedeploy.local");
        tech.setRole(UserRole.TECHNICIAN);
        tech.setActive(true);
        AppUser saved = appUserRepository.save(tech);

        mockMvc.perform(patch("/api/users/technicians/" + saved.getId() + "/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("GET /api/users/me - Obtiene el perfil del usuario actual")
    void getCurrentUser_success() throws Exception {
        AppUser tech = new AppUser();
        tech.setUsername("ana");
        tech.setPassword("techpass");
        tech.setName("Ana García");
        tech.setEmail("ana@bluedeploy.local");
        tech.setRole(UserRole.TECHNICIAN);
        appUserRepository.save(tech);

        mockMvc.perform(get("/api/users/me")
                        .header("X-Username", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana"))
                .andExpect(jsonPath("$.email").value("ana@bluedeploy.local"));
    }

    @Test
    @DisplayName("PUT /api/users/me - Modifica el perfil del usuario actual")
    void updateCurrentUser_success() throws Exception {
        AppUser tech = new AppUser();
        tech.setUsername("ana");
        tech.setPassword("techpass");
        tech.setName("Ana García");
        tech.setEmail("ana@bluedeploy.local");
        tech.setRole(UserRole.TECHNICIAN);
        AppUser saved = appUserRepository.save(tech);

        String payload = """
                {
                  "name": "Ana García Modificada",
                  "email": "ana.mod@bluedeploy.local",
                  "department": "Seguridad Ofensiva"
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana García Modificada"))
                .andExpect(jsonPath("$.email").value("ana.mod@bluedeploy.local"))
                .andExpect(jsonPath("$.department").value("Seguridad Ofensiva"));
    }
}
