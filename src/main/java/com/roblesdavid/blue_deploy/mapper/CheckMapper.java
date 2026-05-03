package com.roblesdavid.blue_deploy.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roblesdavid.blue_deploy.dto.CheckRequestDTO;
import com.roblesdavid.blue_deploy.dto.CheckResponseDTO;
import com.roblesdavid.blue_deploy.model.Check;

import java.util.Map;

public class CheckMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Check toEntity(CheckRequestDTO dto) {

        Check check = new Check();

        check.setHostname(dto.getHostname());
        check.setUser(dto.getUser());
        check.setCreatedAt(dto.getDate().toLocalDateTime());

        Map<String, Object> c = dto.getChecks();

        if (c != null) {
            check.setFirewallEnabled(parseBool(c.get("firewall_enabled")));
            check.setAntivirusEnabled(parseBool(c.get("antivirus_enabled")));
            check.setDiskEncrypted(parseBool(c.get("disk_encrypted")));
            check.setUpdatesEnabled(parseBool(c.get("updates_enabled")));
        }

        check.setRawJson(c != null ? c.toString() : null);

        //JSON correcto
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
        dto.date = check.getCreatedAt().toString();

        dto.firewallEnabled = check.getFirewallEnabled();
        dto.antivirusEnabled = check.getAntivirusEnabled();
        dto.diskEncrypted = check.getDiskEncrypted();
        dto.updatesEnabled = check.getUpdatesEnabled();

        return dto;
    }

    private static Boolean parseBool(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return null;
    }
}
