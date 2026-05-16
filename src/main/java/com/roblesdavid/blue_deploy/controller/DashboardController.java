package com.roblesdavid.blue_deploy.controller;

import com.roblesdavid.blue_deploy.dto.DashboardSummaryDTO;
import com.roblesdavid.blue_deploy.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para exponer métricas agregadas del dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Devuelve el resumen agregado de estados de despliegue y checks críticos.
     *
     * @return métricas globales para el dashboard
     */
    @GetMapping("/summary")
    public DashboardSummaryDTO getSummary() {
        return dashboardService.getSummary();
    }
}
