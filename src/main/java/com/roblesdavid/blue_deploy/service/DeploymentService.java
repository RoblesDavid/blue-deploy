package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.DeploymentRequestDTO;
import com.roblesdavid.blue_deploy.dto.DeploymentResponseDTO;
import com.roblesdavid.blue_deploy.mapper.DeploymentMapper;
import com.roblesdavid.blue_deploy.model.ComplianceStatus;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStatus;
import com.roblesdavid.blue_deploy.repository.DeploymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeploymentService {

    private final DeploymentRepository repository;

    public DeploymentService(DeploymentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DeploymentResponseDTO createDeployment(DeploymentRequestDTO dto) {
        repository.findByDeploymentId(dto.getDeploymentId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un deployment con id: " + dto.getDeploymentId());
        });

        LocalDateTime now = LocalDateTime.now();

        Deployment deployment = new Deployment();
        deployment.setDeploymentId(dto.getDeploymentId());
        deployment.setHostname(dto.getHostname());
        deployment.setDeviceModel(dto.getDeviceModel());
        deployment.setDomainName(dto.getDomainName());
        deployment.setCurrentStep("DEPLOYMENT_CREATED");
        deployment.setDeploymentStatus(DeploymentStatus.IN_PROGRESS);
        deployment.setComplianceStatus(ComplianceStatus.PENDING);
        deployment.setStartedAt(now);
        deployment.setLastSeenAt(now);

        Deployment saved = repository.save(deployment);

        return DeploymentMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<DeploymentResponseDTO> getAllDeployments() {
        return repository.findAll()
                .stream()
                .map(DeploymentMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeploymentResponseDTO getDeploymentByDeploymentId(String deploymentId) {
        Deployment deployment = repository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment no encontrado: " + deploymentId));

        return DeploymentMapper.toDTO(deployment);
    }

    public Deployment getDeploymentEntityByDeploymentId(String deploymentId) {
        return repository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("Deployment no encontrado: " + deploymentId));
    }

    @Transactional
    public Deployment saveDeployment(Deployment deployment) {
        return repository.save(deployment);
    }
}