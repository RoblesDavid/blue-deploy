package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.CheckRequestDTO;
import com.roblesdavid.blue_deploy.service.CheckService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public String health() {
        return "API OK";
    }

    @PostMapping
    public String receiveChecks(@Valid @RequestBody CheckRequestDTO dto) {
        service.processCheck(dto);
        return "Guardado correctamente";
    }
}