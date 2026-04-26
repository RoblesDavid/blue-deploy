package com.roblesdavid.blue_deploy.mapper;

import com.roblesdavid.blue_deploy.dto.CheckRequestDTO;
import com.roblesdavid.blue_deploy.model.Check;

import java.time.LocalDateTime;

/**
 * Mapper manual para convertir DTO → Entity.
 * En proyectos grandes se sustituye por MapStruct.
 */
public class CheckMapper {

    public static Check toEntity(CheckRequestDTO dto) {
        Check check = new Check();

        check.setHostname(dto.getHostname());
        check.setUser(dto.getUser());

        // Convertimos String → LocalDateTime de forma controlada
        check.setCreatedAt(LocalDateTime.parse(dto.getDate().replace(" ", "T")));

        check.setRawJson(dto.getChecks().toString());

        return check;
    }
}
