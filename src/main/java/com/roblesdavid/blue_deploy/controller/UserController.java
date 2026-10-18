package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.CreateTechnicianRequestDTO;
import com.roblesdavid.blue_deploy.dto.UpdateProfileRequestDTO;
import com.roblesdavid.blue_deploy.dto.UserResponseDTO;
import com.roblesdavid.blue_deploy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios, perfiles y técnicos.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Obtiene los datos del perfil del usuario actual (autenticado por JWT o mediante cabeceras opcionales).
     *
     * @param headerUserId identificador opcional en cabecera
     * @param headerUsername nombre de usuario opcional en cabecera
     * @param authentication objeto de autenticación Spring Security
     * @return perfil del usuario
     */
    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestHeader(value = "X-Username", required = false) String headerUsername,
            Authentication authentication
    ) {
        if (headerUserId != null) {
            return userService.getUserProfileById(headerUserId);
        }
        if (headerUsername != null && !headerUsername.isBlank()) {
            return userService.getUserProfileByUsername(headerUsername);
        }
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return userService.getUserProfileByUsername(authentication.getName());
        }
        return userService.getUserProfileByUsername("admin");
    }

    /**
     * Actualiza el perfil del usuario actual (autenticado por JWT o mediante cabeceras opcionales).
     *
     * @param dto datos actualizados del perfil
     * @param headerUserId identificador opcional en cabecera
     * @param headerUsername nombre de usuario opcional en cabecera
     * @param authentication objeto de autenticación Spring Security
     * @return perfil actualizado
     */
    @PutMapping("/me")
    public UserResponseDTO updateCurrentUser(
            @Valid @RequestBody UpdateProfileRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestHeader(value = "X-Username", required = false) String headerUsername,
            Authentication authentication
    ) {
        if (headerUserId != null) {
            return userService.updateUserProfile(headerUserId, dto);
        }
        if (headerUsername != null && !headerUsername.isBlank()) {
            return userService.updateUserProfileByUsername(headerUsername, dto);
        }
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return userService.updateUserProfileByUsername(authentication.getName(), dto);
        }
        return userService.updateUserProfileByUsername("admin", dto);
    }

    /**
     * Devuelve el listado de técnicos del sistema.
     *
     * @return lista de técnicos
     */
    @GetMapping("/technicians")
    public List<UserResponseDTO> getTechnicians() {
        return userService.getAllTechnicians();
    }

    /**
     * Registra un nuevo técnico en el sistema.
     *
     * @param dto datos de alta del técnico
     * @return técnico creado
     */
    @PostMapping("/technicians")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createTechnician(@Valid @RequestBody CreateTechnicianRequestDTO dto) {
        return userService.createTechnician(dto);
    }

    /**
     * Modifica los datos de un técnico existente por ID.
     *
     * @param id identificador del técnico
     * @param dto datos modificados
     * @return técnico actualizado
     */
    @PutMapping("/technicians/{id}")
    public UserResponseDTO updateTechnician(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequestDTO dto
    ) {
        return userService.updateTechnician(id, dto);
    }

    /**
     * Desactiva a un técnico del sistema.
     *
     * @param id identificador del técnico
     * @return técnico con estado active = false
     */
    @PatchMapping("/technicians/{id}/disable")
    public UserResponseDTO disableTechnician(@PathVariable Long id) {
        return userService.disableTechnician(id);
    }
}
