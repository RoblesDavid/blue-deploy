package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.AuditLogResponseDTO;
import com.roblesdavid.blue_deploy.model.AuditAction;
import com.roblesdavid.blue_deploy.model.AuditLog;
import com.roblesdavid.blue_deploy.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio centralizado para el registro y consulta de eventos de auditoría.
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Registra un evento de auditoría en la base de datos de manera resiliente.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAudit(AuditAction action, String actorUsername, String targetId, String targetType, String details, String clientIp) {
        try {
            AuditLog entry = new AuditLog(action, actorUsername, targetId, targetType, details, clientIp);
            auditLogRepository.save(entry);
            log.info("AUDIT [{}]: actor='{}', targetType='{}', targetId='{}', details='{}', ip='{}'",
                    action, actorUsername, targetType, targetId, details, clientIp);
        } catch (Exception ex) {
            log.error("Error al registrar auditoría para acción {}: {}", action, ex.getMessage(), ex);
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAuditLogsByTarget(String targetId) {
        return auditLogRepository.findByTargetIdOrderByCreatedAtDesc(targetId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAuditLogsByActor(String actorUsername) {
        return auditLogRepository.findByActorUsernameOrderByCreatedAtDesc(actorUsername)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AuditLogResponseDTO mapToDTO(AuditLog log) {
        return new AuditLogResponseDTO(
                log.getId(),
                log.getAction(),
                log.getActorUsername(),
                log.getTargetId(),
                log.getTargetType(),
                log.getDetails(),
                log.getClientIp(),
                log.getCreatedAt()
        );
    }
}
