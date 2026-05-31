package com.roblesdavid.blue_deploy.model;

/**
 * Estado individual de una fase reportada del deployment.
 */
public enum StepStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    WARNING,
    FAILED,
    SKIPPED
}
