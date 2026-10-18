## Propósito

Registra los commits recomendados por sprint y sirve como referencia para mantener un historial coherente y útil para la generación del informe final.

---

## Convención de mensajes

```
<tipo>(<alcance>): <descripción en imperativo>
```

| Tipo | Cuándo usarlo |
|------|--------------|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de error |
| `test` | Añadir o modificar tests |
| `chore` | Configuración, dependencias, build |
| `docs` | Actualización de documentación o notas |
| `refactor` | Reestructuración sin cambio de comportamiento |

---

## Commits por sprint

### Sprint 0 — Reorganización
```
docs: add initial project vision and architecture notes
```

### Sprint 1 — Modelo Deployment
```
feat(backend): add Deployment entity, repository, service and REST API
test(backend): add DeploymentApiIntegrationTest with H2 database
```

### Sprint 2 — Registro de fases
```
feat(backend): add DeploymentStep phase tracking with automatic status transitions
test(backend): add DeploymentStepApiIntegrationTest
```

### Sprint 3 — Security Checks
```
feat(backend): add SecurityCheck model with compliance evaluation
test(backend): add SecurityCheckApiIntegrationTest with severity validation
```

### Sprint 4 — Dashboard Summary API
```
feat(backend): expose GET /api/dashboard/summary with 10 aggregated metrics
test(backend): add DashboardApiIntegrationTest
```

### Sprint 5 — Integración PowerShell
```
feat(scripts): add Send-BlueDeployQueue.ps1 and BlueDeployJson.ps1
docs: add PowerShell JSON queue integration notes
```

### Sprint 6 — Dashboard Frontend con Roles

**Commit 1 — Configuración Tailwind:**
```
chore(frontend): install Tailwind CSS v4 and lucide-react icons
```
Ficheros: `package.json`, `vite.config.js`, `src/index.css`

**Commit 2 — Capa de datos mock y autenticación:**
```
feat(frontend): add mock auth system with admin and technician roles
```
Ficheros: `src/data/mockUsers.js`, `src/data/mockDeployments.js`,
`src/auth/AuthContext.jsx`, `src/auth/ProtectedRoute.jsx`

**Commit 3 — Rediseño completo del dashboard:**
```
feat(frontend): redesign dashboard with role-based views and Tailwind UI
```
Ficheros: `src/components/`, `src/pages/admin/`, `src/pages/technician/`, `src/App.jsx`

**Commit 4 — Test de integración con datos realistas:**
```
test(backend): add MockDataPopulationTest with multi-technician deployment scenarios
```
Ficheros: `src/test/.../MockDataPopulationTest.java`

**Commit 5 — Documentación Sprint 6:**
```
docs: update notes for práctica 1 completion and add commit history guide
```
Ficheros: `notes/`, `README.md`, `frontend/README.md`

---

### Sprint 7 — Infraestructura Hetzner (Práctica 1 - Cierre definitivo)

**Commit 1 — Docker frontend + Nginx:**
```
feat(infra): add frontend Dockerfile and nginx reverse proxy config
```
Ficheros: `frontend/Dockerfile`, `nginx/default.conf`

**Commit 2 — Compose producción:**
```
feat(infra): add docker-compose.prod.yml with hardened production configuration
```
Ficheros: `docker-compose.prod.yml`, `.env.production.example`

**Commit 3 — CI/CD GitHub Actions:**
```
ci: add GitHub Actions workflows for automated testing and Hetzner deployment
```
Ficheros: `.github/workflows/ci.yml`, `.github/workflows/deploy.yml`

**Commit 4 — Script de setup del servidor:**
```
feat(infra): add Hetzner server setup script with Docker and UFW configuration
```
Ficheros: `scripts/deploy/setup-hetzner.sh`

**Commit 5 — Documentación infraestructura:**
```
docs: add Hetzner deployment guide and update gitignore
```
Ficheros: `notes/13 Despliegue en Hetzner.md`, `.gitignore`, `README.md`

---

### Sprint 8 — Persistencia de Usuarios y Gestión de Técnicos (Práctica 2)

**Commit 1 — Modelo y Repositorio:**
```
feat(backend): add AppUser entity, UserRole enum and AppUserRepository
```
Ficheros: `src/main/.../model/UserRole.java`, `src/main/.../model/AppUser.java`, `src/main/.../repository/AppUserRepository.java`

**Commit 2 — DTOs y Mappers:**
```
feat(backend): add User DTOs and UserMapper for request/response handling
```
Ficheros: `src/main/.../dto/UserResponseDTO.java`, `src/main/.../dto/CreateTechnicianRequestDTO.java`, `src/main/.../dto/UpdateProfileRequestDTO.java`, `src/main/.../mapper/UserMapper.java`

**Commit 3 — Servicio de Usuarios:**
```
feat(backend): implement UserService with technician management and profile updates
```
Ficheros: `src/main/.../service/UserService.java`

**Commit 4 — Controlador REST:**
```
feat(backend): expose UserController endpoints for profile and technician operations
```
Ficheros: `src/main/.../controller/UserController.java`

**Commit 5 — Tests de Integración H2:**
```
test(backend): add UserApiIntegrationTest covering technician lifecycle and profile updates
```
Ficheros: `src/test/.../integration/BaseApiIntegrationTest.java`, `src/test/.../integration/UserApiIntegrationTest.java`

**Commit 6 — Documentación y Seguridad:**
```
docs: add user management notes and update development plan for Sprint 8
```
Ficheros: `notes/04 Plan de Desarrollo.md`, `notes/06 Roadmap Práctica 2.md`, `notes/12 Historial de Commits y Estrategia Git.md`, `notes/14 Gestion de Usuarios y Roles Backend.md`, `.gitignore`

---

### Sprint 9 — Autenticación JWT y Spring Security (Práctica 2)

**Commit 1 — Dependencias y Configuración de Propiedades:**
```
chore(security): add spring security, jjwt dependencies and jwt configuration properties
```
Ficheros: `pom.xml`, `src/main/resources/application.properties`, `src/test/resources/application-test.properties`

**Commit 2 — DTOs de Autenticación:**
```
feat(auth): create LoginRequestDTO and LoginResponseDTO
```
Ficheros: `src/main/.../dto/LoginRequestDTO.java`, `src/main/.../dto/LoginResponseDTO.java`

**Commit 3 — Servicios y Filtro de Seguridad JWT:**
```
feat(security): implement JwtService, CustomUserDetailsService and JwtAuthenticationFilter
```
Ficheros: `src/main/.../security/JwtService.java`, `src/main/.../security/CustomUserDetails.java`, `src/main/.../security/CustomUserDetailsService.java`, `src/main/.../security/JwtAuthenticationFilter.java`

**Commit 4 — Configuración de Spring Security y Cifrado BCrypt:**
```
feat(security): configure SecurityConfig with stateless session, role authorization and BCrypt
```
Ficheros: `src/main/.../config/SecurityConfig.java`, `src/main/.../service/UserService.java`, `src/main/.../controller/UserController.java`

**Commit 5 — Endpoint de Login y Manejo Global de Excepciones:**
```
feat(auth): add POST /api/auth/login endpoint and security exception handling
```
Ficheros: `src/main/.../service/AuthService.java`, `src/main/.../controller/AuthController.java`, `src/main/.../exception/GlobalExceptionHandler.java`

**Commit 6 — Tests de Integración de Autenticación y Autorización:**
```
test(auth): add AuthApiIntegrationTest covering login, JWT verification and role access control
```
Ficheros: `src/test/.../integration/AuthApiIntegrationTest.java`, `src/test/.../integration/UserApiIntegrationTest.java`

**Commit 7 — Documentación Sprint 9:**
```
docs: add JWT authentication and security architecture notes
```
Ficheros: `notes/15 Autenticacion JWT y Seguridad Spring Security.md`, `notes/12 Historial de Commits y Estrategia Git.md`

---

### Sprint 10 — Asignación de Deployments a Técnicos y Filtrado por Rol

**Commit 1 — Relación de Modelo, DTOs y Mapper:**
```
feat(backend): relate Deployment with AppUser technician and expose technician fields in DTOs
```
Ficheros: `src/main/.../model/Deployment.java`, `src/main/.../dto/DeploymentRequestDTO.java`, `src/main/.../dto/DeploymentResponseDTO.java`, `src/main/.../mapper/DeploymentMapper.java`

**Commit 2 — Repositorio y Servicio de Deployments:**
```
feat(backend): implement technician assignment, role-based deployment query and /my endpoint logic
```
Ficheros: `src/main/.../repository/DeploymentRepository.java`, `src/main/.../service/DeploymentService.java`

**Commit 3 — Controlador REST y Reglas de Seguridad:**
```
feat(backend): expose PUT /assign-technician and GET /my with role-based security rules
```
Ficheros: `src/main/.../controller/DeploymentController.java`, `src/main/.../config/SecurityConfig.java`

**Commit 4 — Tests de Integración:**
```
test(backend): add integration tests for deployment technician assignment and role filtering
```
Ficheros: `src/test/.../integration/DeploymentApiIntegrationTest.java`

**Commit 5 — Documentación y Guía Frontend:**
```
docs: add deployment assignment documentation and frontend integration roadmap
```
Ficheros: `notes/16 Asignacion de Deployments y Filtro por Rol.md`, `notes/12 Historial de Commits y Estrategia Git.md`

---

### Sprint 11 — Protección de Endpoints de Ingesta con API Key

**Commit 1 — Filtro de Seguridad y Configuración de Propiedades:**
```
feat(security): implement ApiKeyAuthenticationFilter and configure bluedeploy.api-key
```
Ficheros: `src/main/.../security/ApiKeyAuthenticationFilter.java`, `src/main/resources/application.properties`, `src/test/resources/application-test.properties`, `docker-compose.yml`, `docker-compose.prod.yml`

**Commit 2 — Reglas de Seguridad en SecurityConfig:**
```
feat(security): protect POST endpoints for deployments, steps and checks with API Key and Admin role
```
Ficheros: `src/main/.../config/SecurityConfig.java`

**Commit 3 — Actualización de Script PowerShell:**
```
feat(scripts): update Send-BlueDeployQueue.ps1 to send X-BlueDeploy-Api-Key header
```
Ficheros: `scripts/powershell/bluedeploy/Send-BlueDeployQueue.ps1`

**Commit 4 — Tests de Integración para API Key:**
```
test(security): add ApiKeyIntegrationTest and update base integration tests with API key header
```
Ficheros: `src/test/.../integration/ApiKeyIntegrationTest.java`, `src/test/.../integration/BaseApiIntegrationTest.java`, `src/test/.../integration/DeploymentApiIntegrationTest.java`, `src/test/.../integration/DeploymentStepApiIntegrationTest.java`, `src/test/.../integration/SecurityCheckApiIntegrationTest.java`, `src/test/.../integration/DeploymentFlowIntegrationTest.java`, `src/test/.../integration/MockDataPopulationTest.java`, `src/test/.../integration/DashboardApiIntegrationTest.java`

**Commit 5 — Documentación Sprint 11:**
```
docs: add API Key endpoint protection documentation and update commit history
```
Ficheros: `notes/17 Proteccion de Endpoints con API Key.md`, `notes/12 Historial de Commits y Estrategia Git.md`

---

## Flujo de ramas y tags

```bash
# Integrar sprint en develop
git checkout develop
git merge --no-ff feature/api-key-protection -m "Merge Sprint 11 - Protección de Endpoints de Ingesta con API Key"

# Liberar en main con tag semántico
git checkout main
git merge --no-ff develop
git tag -a v1.4.0 -m "Práctica 2 - Protección de Ingesta con API Key"
git push origin main --tags
```

## Ramas activas

| Rama | Estado | Descripción |
|------|--------|-------------|
| main | Estable | Versión de producción |
| develop | Desarrollo | Integración de sprints |
| feature/api-key-protection | En curso | Sprint 11 — Protección de Endpoints de Ingesta con API Key |
