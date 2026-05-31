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

## Flujo de ramas y tags

```bash
# Integrar sprint en develop
git checkout develop
git merge --no-ff feature/dashboard-summary -m "Merge práctica 1 completa con infraestructura Hetzner"

# Liberar en main con tag semántico
git checkout main
git merge --no-ff develop
git tag -a v1.0.0 -m "Práctica 1 - BlueDeploy con despliegue en Hetzner"
git push origin main --tags
```

## Ramas activas

| Rama | Estado | Descripción |
|------|--------|-------------|
| main | Estable | Versión de producción |
| develop | Desarrollo | Integración de sprints |
| feature/dashboard-summary | En curso | Sprint 6+7 — Frontend y despliegue |
