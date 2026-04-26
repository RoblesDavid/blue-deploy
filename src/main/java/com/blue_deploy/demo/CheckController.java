package com.blue_deploy.demo;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checks")
public class CheckController {

    @GetMapping
    public String getChecks() {
        return "API OK";
    }

    @PostMapping
    public String receiveChecks(@RequestBody String payload) {
        System.out.println("📩 JSON recibido:");
        System.out.println(payload);

        return "Datos recibidos";
    }
}