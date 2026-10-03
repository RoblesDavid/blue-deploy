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

## Flujo de ramas y tags

```bash
# Integrar sprint en develop
git checkout develop
git merge --no-ff feature/user-management -m "Merge Sprint 8 - Persistencia de usuarios y técnicos"

# Liberar en main con tag semántico
git checkout main
git merge --no-ff develop
git tag -a v1.1.0 -m "Práctica 2 - Persistencia de usuarios y endpoints de gestión"
git push origin main --tags
```

## Ramas activas

| Rama | Estado | Descripción |
|------|--------|-------------|
| main | Estable | Versión de producción |
| develop | Desarrollo | Integración de sprints |
| feature/user-management | En curso | Sprint 8 — Persistencia de usuarios y gestión de técnicos |
