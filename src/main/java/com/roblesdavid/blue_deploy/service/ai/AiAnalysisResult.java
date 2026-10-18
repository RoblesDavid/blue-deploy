package com.roblesdavid.blue_deploy.service.ai;

import java.util.List;

/**
 * Resultado estructurado devuelto por el proveedor de análisis de IA.
 */
public record AiAnalysisResult(
        String summary,
        String probableCause,
        List<String> recommendedActions,
        String severity
) {}
