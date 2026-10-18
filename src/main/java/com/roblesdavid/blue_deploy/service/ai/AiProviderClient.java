package com.roblesdavid.blue_deploy.service.ai;

import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStep;
import com.roblesdavid.blue_deploy.model.SecurityCheck;

import java.util.List;

/**
 * Contrato para el proveedor de análisis de diagnósticos con Inteligencia Artificial.
 */
public interface AiProviderClient {

    /**
     * Analiza el estado, fases fallidas y checks no conformes de un despliegue
     * generando un diagnóstico con causa probable y remediaciones.
     */
    AiAnalysisResult analyzeDeployment(Deployment deployment, List<DeploymentStep> steps, List<SecurityCheck> checks);
}
