package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.CheckRequestDTO;
import com.roblesdavid.blue_deploy.model.Check;
import com.roblesdavid.blue_deploy.service.CheckService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<Check> getAllChecks(){
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
     * Healthcheck separado (best practice)
     */
    @GetMapping("/health")
    public String health() {
        return "API OK";
    }
}