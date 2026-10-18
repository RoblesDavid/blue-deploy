# Roadmap Práctica 2 — BlueDeploy

## Objetivo general

Evolucionar BlueDeploy desde una herramienta de monitorización de despliegues OSDCloud hacia una plataforma cliente/servidor integral con validación avanzada de seguridad, control de acceso basado en roles (RBAC), auditoría continua y automatización asistida por Inteligencia Artificial.

---

## Plan de ejecución Práctica 2

### Sprint P2-1 - Usuarios persistidos y gestión de técnicos
- **Objetivo**: Reemplazar usuarios mock por usuarios reales persistidos en base de datos MariaDB/H2.
- **Tareas implementadas**:
  - Entidad `AppUser` y enum `UserRole` (`ADMIN`, `TECHNICIAN`).
  - Repositorio `AppUserRepository`.
  - DTOs de usuario (`UserResponseDTO`, `CreateTechnicianRequestDTO`, `UpdateProfileRequestDTO`).
  - `UserService` y `UserController`.
  - Batería de tests de integración con H2.
- **Resultado**: Gestión completa de administradores y técnicos en base de datos relacional.

---

### Sprint P2-2 - Autenticación JWT y Spring Security
- **Objetivo**: Añadir autenticación real y autorización basada en roles (RBAC).
- **Tareas implementadas**:
  - Configuración stateless con Spring Security (`SecurityConfig`).
  - `JwtService` y filtro `JwtAuthenticationFilter`.
  - `CustomUserDetailsService` y `PasswordEncoder` con BCrypt.
  - Endpoint público `POST /api/auth/login`.
  - Protección granular por roles y tests de escenarios 401/403.
- **Resultado**: Emisión y validación de tokens JWT seguros.

---

### Sprint P2-3 - Deployments asignados a técnicos
- **Objetivo**: Relacionar despliegues con técnicos reales y visibilidad por rol.
- **Tareas implementadas**:
  - Relación JPA entre `Deployment` y `AppUser`.
  - DTOs actualizados (`technicianId`, `technicianName`, `technicianUsername`).
  - Asignación de técnico al crear o reasignar (`PUT /api/deployments/{id}/assign-technician/{techId}`).
  - Filtrado de consultas: `ADMIN` visualiza todos los despliegues; `TECHNICIAN` solo los asignados.
  - Endpoint `GET /api/deployments/my`.
- **Resultado**: Aislamiento estricto de visibilidad y propiedad de despliegues.

---

### Sprint P2-4 - API Key para scripts PowerShell
- **Objetivo**: Proteger endpoints de ingesta de eventos OSDCloud.
- **Tareas implementadas**:
  - Variable de entorno `BLUEDEPLOY_API_KEY`.
  - Filtro `ApiKeyAuthenticationFilter` validando cabecera `X-BlueDeploy-Api-Key`.
  - Protección de endpoints `/api/deployments`, `/steps` y `/checks`.
  - `Send-BlueDeployQueue.ps1` adaptado para enviar la cabecera.
  - Sanitización de contraseñas en scripts PowerShell.
- **Resultado**: Ingesta autenticada sin secretos hardcodeados.

---

### Sprint P2-5 - Frontend conectado al backend real
- **Objetivo**: Conectar React/Vite directamente al backend de Spring Boot.
- **Tareas implementadas**:
  - `apiClient.js` con inyección de Bearer Token y gestión de 401.
  - Módulos `authApi`, `usersApi`, `deploymentsApi`, `dashboardApi` y `auditApi`.
  - `AuthContext` y `Login.jsx` con autenticación real y localStorage.
  - Adaptación de vistas: `AdminDashboard`, `TechnicianDashboard`, `AllDeployments`, `MyDeployments`, `TechnicianManagement`, `MyProfile`.
  - Loading states y manejo de errores.
- **Resultado**: Flujo E2E conectado y operativo.

---

### Sprint P2-6 - Dashboard summary sensible al rol y métricas
- **Objetivo**: Métricas del dashboard adaptadas al rol del usuario.
- **Tareas implementadas**:
  - `DashboardService.getSummaryForUser(auth)` con métricas globales para admin y acotadas para técnicos.
  - Nuevas métricas: tiempo medio, técnicos activos, última alerta y fallo reciente.
- **Resultado**: Dashboard contextual y analítico.

---

### Sprint P2-7 - Auditoría de acciones
- **Objetivo**: Registrar eventos clave de seguridad y negocio.
- **Tareas implementadas**:
  - Entidad `AuditLog`, enum `AuditAction`, repositorio y servicio `AuditLogService`.
  - Controlador `AuditLogController` exclusivo para `ADMIN`.
  - Auditoría de logins, creación de técnicos, perfiles, asignaciones y fallos de API Key.
- **Resultado**: Trazabilidad completa de auditoría.

---

### Sprint P2-8 - Exportación de informes PDF
- **Objetivo**: Generar informes técnicos ejecutivos en PDF.
- **Tareas implementadas**:
  - `DeploymentReportService` con OpenPDF.
  - Endpoint `GET /api/deployments/{id}/report.pdf` protegido por rol y propiedad.
  - Botón de descarga en frontend con blob streaming.
- **Resultado**: Informes PDF de cumplimiento y trazabilidad.

---

### Sprint P2-9 - Diagnóstico de incidencias con IA
- **Objetivo**: Resumen automático de errores OSDCloud/EDR y propuesta de mitigación.
- **Tareas implementadas**:
  - Entidad `AiSummary`, cliente `AiProviderClient` (heurística y mock de Blue Team).
  - Endpoints `POST /api/deployments/{id}/ai-summary` y `GET /api/deployments/{id}/ai-summary`.
  - Panel visual de severidad y recomendaciones en `DeploymentDetail.jsx`.
- **Resultado**: Diagnósticos accionables para el equipo de soporte.

---

### Sprint P2-10 - Hardening de producción en Hetzner
- **Objetivo**: Blindar la infraestructura de despliegue en servidor cloud.
- **Tareas implementadas**:
  - Nginx con cabeceras CSP, HSTS, X-Frame-Options, X-Content-Type-Options y proxy aislado.
  - `docker-compose.prod.yml` con aislamiento de puertos (MariaDB y backend solo en red interna).
  - `.env.production.example` con rotación y generación criptográfica de secretos.
  - Script de instalación `setup-hetzner.sh` con UFW y backups periódicos de MariaDB.
- **Resultado**: Entorno de producción hardened y resiliente.

---

### Sprint P2-11 - Landing Page & Portada de Introducción
- **Objetivo**: Crear página de aterrizaje profesional con resumen ejecutivo del proyecto, arquitectura y datos de contacto del autor.
- **Tareas implementadas**:
  - `LandingPage.jsx` con navegación fluida, métricas destacadas y visualización del flujo OSDCloud en 4 fases.
  - Sección de contacto con datos de David Robles Gaona (Teléfono: 629096538, perfil de LinkedIn).
  - Enrutamiento dinámico en `App.jsx` y retorno fluido desde `Login.jsx`.
- **Resultado**: Presentación profesional del proyecto y acceso directo a la consola de administración.
