package com.roblesdavid.blue_deploy.exception;

import java.time.LocalDateTime;

/**
 * Representa un error estándar devuelto por la API.
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
