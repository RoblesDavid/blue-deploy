package com.roblesdavid.blue_deploy.model;

/**
 * Estados de evolución del proceso de deployment.
 */
public enum DeploymentStatus {
    IN_PROGRESS,
    DOMAIN_JOINED,
    POST_DOMAIN_PENDING,
    COMPLETED,
    COMPLETED_WITH_WARNINGS,
    FAILED,
    NON_COMPLIANT
}
