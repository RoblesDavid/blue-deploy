package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.LoginRequestDTO;
import com.roblesdavid.blue_deploy.dto.LoginResponseDTO;
import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.AuditAction;
import com.roblesdavid.blue_deploy.repository.AppUserRepository;
import com.roblesdavid.blue_deploy.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de negocio encargado de la autenticación de usuarios y emisión de JWT.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository userRepository;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public AuthService(AuthenticationManager authenticationManager,
                       AppUserRepository userRepository,
                       JwtService jwtService,
                       AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        String clientIp = getClientIp();
        try {
            AppUser user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

            if (!user.isActive()) {
                auditLogService.recordAudit(
                        AuditAction.LOGIN_FAILED,
                        request.getUsername(),
                        String.valueOf(user.getId()),
                        "USER",
                        "Intento de inicio de sesión en cuenta desactivada",
                        clientIp
                );
                throw new DisabledException("La cuenta de usuario está desactivada");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("id", user.getId());
            extraClaims.put("name", user.getName());
            extraClaims.put("email", user.getEmail());

            String token = jwtService.generateToken(extraClaims, user.getUsername());

            auditLogService.recordAudit(
                    AuditAction.LOGIN_SUCCESS,
                    user.getUsername(),
                    String.valueOf(user.getId()),
                    "USER",
                    "Inicio de sesión exitoso con rol " + user.getRole(),
                    clientIp
            );

            return new LoginResponseDTO(
                    token,
                    "Bearer",
                    jwtService.getExpirationTime(),
                    user.getId(),
                    user.getUsername(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getInitials(),
                    user.getDepartment()
            );
        } catch (BadCredentialsException ex) {
            auditLogService.recordAudit(
                    AuditAction.LOGIN_FAILED,
                    request.getUsername(),
                    null,
                    "AUTH",
                    "Credenciales incorrectas",
                    clientIp
            );
            throw ex;
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String xfHeader = req.getHeader("X-Forwarded-For");
                if (xfHeader != null && !xfHeader.isBlank()) {
                    return xfHeader.split(",")[0].trim();
                }
                return req.getRemoteAddr();
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}
