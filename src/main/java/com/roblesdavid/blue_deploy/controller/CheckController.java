package com.roblesdavid.blue_deploy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roblesdavid.blue_deploy.model.Check;
import com.roblesdavid.blue_deploy.repository.CheckRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/checks")
public class CheckController {

    private final CheckRepository repository;
    private final ObjectMapper objectMapper;

    public CheckController(CheckRepository repository, ObjectMapper objectMapper){
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String getChecks() {
        return "API OK";
    }


    @PostMapping
    public String receiveChecks(@RequestBody String payload) {
        try {
            System.out.println("📩 JSON recibido:");
            System.out.println(payload);

            JsonNode json = objectMapper.readTree(payload);

            String hostname = json.has("hostname") ? json.get("hostname").asText() : "UNKNOWN";
            String user = json.has("user") ? json.get("user").asText() : "UNKNOWN";
            String dateStr = json.has("date") ? json.get("date").asText() : null;

            // Convertir fecha "2026-04-26 17:00:56" → LocalDateTime
            LocalDateTime createdAt;

            try {
                if (dateStr != null) {
                    createdAt = LocalDateTime.parse(dateStr.replace(" ", "T"));
                } else {
                    createdAt = LocalDateTime.now();
                }
            } catch (Exception e) {
                createdAt = LocalDateTime.now();
            }

            Check check = new Check();
            check.setHostname(hostname);
            check.setUser(user);
            check.setCreatedAt(createdAt);
            check.setRawJson(payload);

            Check saved = repository.save(check);

            return "Guardado en DB" + saved.getId();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error procesando JSON";
        }
    }
}