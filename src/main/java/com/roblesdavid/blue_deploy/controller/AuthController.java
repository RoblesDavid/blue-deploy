package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.LoginRequestDTO;
import com.roblesdavid.blue_deploy.dto.LoginResponseDTO;
import com.roblesdavid.blue_deploy.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para autenticación y login con JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint público para iniciar sesión y obtener el token JWT.
     *
     * @param request credenciales del usuario
     * @return token y datos del usuario autenticado
     */
    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
