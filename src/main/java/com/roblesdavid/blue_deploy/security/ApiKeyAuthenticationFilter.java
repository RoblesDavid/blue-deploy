package com.roblesdavid.blue_deploy.security;

import com.roblesdavid.blue_deploy.model.AuditAction;
import com.roblesdavid.blue_deploy.service.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Filtro HTTP para validar la cabecera X-BlueDeploy-Api-Key en endpoints de ingesta.
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-BlueDeploy-Api-Key";

    private final String configuredApiKey;
    private final AuditLogService auditLogService;

    public ApiKeyAuthenticationFilter(
            @Value("${bluedeploy.api-key:}") String configuredApiKey,
            AuditLogService auditLogService
    ) {
        this.configuredApiKey = configuredApiKey;
        this.auditLogService = auditLogService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestApiKey = request.getHeader(API_KEY_HEADER);

        if (requestApiKey != null) {
            if (configuredApiKey != null && !configuredApiKey.isBlank() && configuredApiKey.equals(requestApiKey)) {
                SecurityContext originalContext = SecurityContextHolder.getContext();
                try {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            "api-client",
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
                    );
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                    filterChain.doFilter(request, response);
                } finally {
                    SecurityContextHolder.setContext(originalContext);
                }
                return;
            } else {
                String clientIp = request.getHeader("X-Forwarded-For");
                if (clientIp == null || clientIp.isBlank()) {
                    clientIp = request.getRemoteAddr();
                } else {
                    clientIp = clientIp.split(",")[0].trim();
                }

                auditLogService.recordAudit(
                        AuditAction.INVALID_API_KEY,
                        "anonymous",
                        request.getRequestURI(),
                        "API_KEY",
                        "Intento de acceso con API Key inválida en " + request.getRequestURI(),
                        clientIp
                );

                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"No autorizado\",\"message\":\"API Key no válida\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
