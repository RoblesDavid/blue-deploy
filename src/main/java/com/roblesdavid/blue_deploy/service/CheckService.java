package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.CheckRequestDTO;
import com.roblesdavid.blue_deploy.dto.CheckResponseDTO;
import com.roblesdavid.blue_deploy.mapper.CheckMapper;
import com.roblesdavid.blue_deploy.model.Check;
import com.roblesdavid.blue_deploy.repository.CheckRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio encargado de la lógica de negocio de los checks.
 */
@Service
public class CheckService {

    private final CheckRepository repository;
    private final AlertService alertService;

    public CheckService(CheckRepository repository, AlertService alertService) {

        this.repository = repository;
        this.alertService=alertService;
    }

    /**
     * Procesa un DTO validado y lo guarda en base de datos.
     */
    public void processCheck(CheckRequestDTO dto) {
        Check check = CheckMapper.toEntity(dto);
        repository.save(check);

        //Análisis automático
        alertService.analyze(check);
    }

    /**
     * Devuelve todos los checks para el frontend.
     */
    public List<CheckResponseDTO> getAllChecks() {
        return repository.findAll()
                .stream()
                .map(CheckMapper::toDTO)
                .toList();
    }
}