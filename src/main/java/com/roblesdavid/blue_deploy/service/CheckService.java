package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.CheckRequestDTO;
import com.roblesdavid.blue_deploy.mapper.CheckMapper;
import com.roblesdavid.blue_deploy.model.Check;
import com.roblesdavid.blue_deploy.repository.CheckRepository;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la lógica de negocio de los checks.
 */
@Service
public class CheckService {

    private final CheckRepository repository;

    public CheckService(CheckRepository repository) {
        this.repository = repository;
    }

    /**
     * Procesa un DTO validado y lo guarda en base de datos.
     */
    public void processCheck(CheckRequestDTO dto) {
        Check check = CheckMapper.toEntity(dto);
        repository.save(check);
    }
}