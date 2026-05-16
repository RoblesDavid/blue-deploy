package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.CheckRequestDTO;
import com.roblesdavid.blue_deploy.dto.CheckResponseDTO;
import com.roblesdavid.blue_deploy.service.CheckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller: capa HTTP pura.
 * No contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/checks")
public class CheckController {

    private final CheckService service;

    public CheckController(CheckService service) {
        this.service = service;
    }

    /**
     * GET → devuelve los checks para el frontend
     */
    @GetMapping
    public List<CheckResponseDTO> getAllChecks() {
        return service.getAllChecks();
    }

    /**
     * POST → recibe checks
     */
    @PostMapping
    public String receiveChecks(@Valid @RequestBody CheckRequestDTO dto) {
        service.processCheck(dto);
        return "Guardado correctamente";
    }

    /**
     * Login simple (mock)
     */
    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> body) {

        if ("admin".equals(body.get("user")) &&
                "admin".equals(body.get("password"))) {
            return "TOKEN_OK";
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    /**
     * Healthcheck separado (best practice)
     */
    @GetMapping("/health")
    public String health() {
        return "API OK";
    }
}
