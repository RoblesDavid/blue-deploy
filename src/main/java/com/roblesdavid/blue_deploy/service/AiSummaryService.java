package com.roblesdavid.blue_deploy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roblesdavid.blue_deploy.dto.AiSummaryDTO;
import com.roblesdavid.blue_deploy.model.AiSummary;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStep;
import com.roblesdavid.blue_deploy.model.SecurityCheck;
import com.roblesdavid.blue_deploy.repository.AiSummaryRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentStepRepository;
import com.roblesdavid.blue_deploy.repository.SecurityCheckRepository;
import com.roblesdavid.blue_deploy.service.ai.AiAnalysisResult;
import com.roblesdavid.blue_deploy.service.ai.AiProviderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para generar y persistir resúmenes de diagnóstico con IA.
 */
@Service
public class AiSummaryService {

    private static final Logger log = LoggerFactory.getLogger(AiSummaryService.class);

    private final AiSummaryRepository aiSummaryRepository;
    private final DeploymentRepository deploymentRepository;
    private final DeploymentStepRepository stepRepository;
    private final SecurityCheckRepository checkRepository;
    private final AiProviderClient aiProviderClient;
    private final ObjectMapper objectMapper;

    public AiSummaryService(
            AiSummaryRepository aiSummaryRepository,
            DeploymentRepository deploymentRepository,
            DeploymentStepRepository stepRepository,
            SecurityCheckRepository checkRepository,
            AiProviderClient aiProviderClient,
            ObjectMapper objectMapper
    ) {
        this.aiSummaryRepository = aiSummaryRepository;
        this.deploymentRepository = deploymentRepository;
        this.stepRepository = stepRepository;
        this.checkRepository = checkRepository;
        this.aiProviderClient = aiProviderClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AiSummaryDTO generateSummary(String deploymentId) {
        Deployment deployment = deploymentRepository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment no encontrado: " + deploymentId));

        List<DeploymentStep> steps = stepRepository.findByDeploymentOrderByReportedAtAsc(deployment);
        List<SecurityCheck> checks = checkRepository.findByDeploymentOrderByReportedAtAsc(deployment);

        AiAnalysisResult analysis = aiProviderClient.analyzeDeployment(deployment, steps, checks);

        String jsonActions = "[]";
        try {
            jsonActions = objectMapper.writeValueAsString(analysis.recommendedActions());
        } catch (Exception ex) {
            log.warn("Error serializando acciones recomendadas: {}", ex.getMessage());
        }

        AiSummary summaryEntity = new AiSummary(
                deploymentId,
                analysis.summary(),
                analysis.probableCause(),
                jsonActions,
                analysis.severity()
        );

        AiSummary saved = aiSummaryRepository.save(summaryEntity);
        log.info("Generado resumen IA para deployment '{}' con severidad '{}'", deploymentId, analysis.severity());

        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public Optional<AiSummaryDTO> getLatestSummary(String deploymentId) {
        return aiSummaryRepository.findTopByDeploymentIdOrderByCreatedAtDesc(deploymentId)
                .map(this::mapToDTO);
    }

    private AiSummaryDTO mapToDTO(AiSummary entity) {
        List<String> actions = new ArrayList<>();
        if (entity.getRecommendedActions() != null && !entity.getRecommendedActions().isBlank()) {
            try {
                actions = objectMapper.readValue(entity.getRecommendedActions(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                actions = List.of(entity.getRecommendedActions());
            }
        }

        return new AiSummaryDTO(
                entity.getId(),
                entity.getDeploymentId(),
                entity.getSummary(),
                entity.getProbableCause(),
                actions,
                entity.getSeverity(),
                entity.getCreatedAt()
        );
    }
}
