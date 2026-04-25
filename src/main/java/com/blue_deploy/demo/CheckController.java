package com.blue_deploy.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckController {

    @GetMapping("/api/checks")
    public String getChecks() {
        return "API OK";
    }
}
