package com.roblesdavid.blue_deploy.model;

/**
 * Tipos de acciones auditables en el ciclo de vida del sistema BlueDeploy.
 */
public enum AuditAction {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    TECHNICIAN_CREATED,
    PROFILE_UPDATED,
    DEPLOYMENT_ASSIGNED,
    SCRIPT_EVENT_RECEIVED,
    INVALID_API_KEY,
    DEPLOYMENT_STATUS_CHANGED,
    COMPLIANCE_STATUS_CHANGED
}
