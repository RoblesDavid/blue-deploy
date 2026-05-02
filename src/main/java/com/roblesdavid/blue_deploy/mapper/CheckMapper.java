package com.roblesdavid.blue_deploy.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roblesdavid.blue_deploy.dto.CheckRequestDTO;
import com.roblesdavid.blue_deploy.dto.CheckResponseDTO;
import com.roblesdavid.blue_deploy.model.Check;

public class CheckMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Check toEntity(CheckRequestDTO dto) {

        Check check = new Check();

        check.setHostname(dto.getHostname());
        check.setUser(dto.getUser());
        check.setCreatedAt(dto.getDate().toLocalDateTime());

        // 🔥 Parse seguro
        Boolean firewall = null;

        if (dto.getChecks() != null) {
            Object value = dto.getChecks().get("firewall_enabled");

            if (value instanceof Boolean) {
                firewall = (Boolean) value;
            } else if (value instanceof String) {
                firewall = Boolean.parseBoolean((String) value);
            }
        }

        check.setFirewallEnabled(firewall);

        // 🔥 JSON correcto
        try {
            check.setRawJson(mapper.writeValueAsString(dto.getChecks()));
        } catch (JsonProcessingException e) {
            check.setRawJson("{}"); // fallback seguro
        }

        return check;
    }

    public static CheckResponseDTO toDTO(Check check) {

        CheckResponseDTO dto = new CheckResponseDTO();

        dto.id = check.getId();
        dto.hostname = check.getHostname();
        dto.user = check.getUser();
        dto.date = check.getCreatedAt() != null
                ? check.getCreatedAt().toString()
                : null;
        dto.firewallEnabled = check.getFirewallEnabled();

        return dto;
    }
}
