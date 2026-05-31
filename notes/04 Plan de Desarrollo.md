## Sprint 0 - Reorganización del proyecto

Objetivo:

- Reorientar BlueDeploy hacia monitorización de despliegues Windows.
- Crear notas técnicas.
- Actualizar README.
- Definir modelo de dominio.

Resultado esperado:

- Carpeta notes creada.
- Documentación inicial preparada.
- Nueva visión del proyecto definida.

---

## Sprint 1 - Modelo Deployment

Objetivo:

Crear el modelo base para registrar despliegues.

Resultado:

- Entidad Deployment creada.
- Estados DeploymentStatus y ComplianceStatus creados.
- API REST para crear y consultar despliegues.
- Evidencia registrada en [[notes/08-evidencias.md]].

---

## Sprint 2 - Registro de fases

Objetivo:

Registrar cada fase del despliegue enviada desde PowerShell.

Tareas completadas:

- DeploymentStep creado.
- StepStatus creado.
- DeploymentStepRepository creado.
- DeploymentStepRequestDTO creado.
- DeploymentStepResponseDTO creado.
- DeploymentStepService creado.
- DeploymentStepController creado.
- Actualización automática de currentStep.
- Actualización automática de lastSeenAt.
- Cambio automático a DOMAIN_JOINED tras DOMAIN_JOIN correcto.
- Cambio automático a POST_DOMAIN_PENDING tras GPUPDATE correcto.
- Cambio automático a FAILED ante cualquier fase fallida.

Endpoints implementados:

- POST /api/deployments/{deploymentId}/steps
- GET /api/deployments/{deploymentId}/steps

Estados automáticos:

- DOMAIN_JOIN + SUCCESS -> DOMAIN_JOINED
- GPUPDATE + SUCCESS -> POST_DOMAIN_PENDING
- FINAL_SECURITY_VALIDATION + SUCCESS -> COMPLETED
- Cualquier fase + FAILED -> FAILED

Resultado:

BlueDeploy puede registrar el progreso de un despliegue OSDCloud por fases y consultar el histórico de pasos de cada equipo.

---

## Sprint 3 - Checks de seguridad

Objetivo:

Registrar validaciones post-dominio.

Estado: COMPLETADO

Tareas completadas:

- SecurityCheck y enums de estado/severidad.
- SecurityCheckRepository.
- SecurityCheckService.
- SecurityCheckController.
- Evaluación automática de compliance.
- Manejo consistente de errores API.
- Tests de integración para casos OK y casos de error.

Endpoints implementados:

- POST /api/deployments/{deploymentId}/checks
- GET /api/deployments/{deploymentId}/checks

Checks iniciales:

- FIREWALL_ENABLED
- WSUS_CONFIGURED
- CROWDSTRIKE_VALIDATED
- MICROCLAUDIA_VALIDATED
- LOCAL_ADMIN_VALIDATED

---

## Sprint 4 - Dashboard Summary API

Objetivo:

Exponer métricas agregadas para alimentar el dashboard web.

Estado: COMPLETADO

Endpoint implementado:

- GET /api/dashboard/summary

Métricas implementadas:

- totalDeployments
- inProgress
- domainJoined
- postDomainPending
- completed
- completedWithWarnings
- failed
- nonCompliant
- pendingCrowdStrike
- pendingMicroClaudia

Resultado:

BlueDeploy ya expone un resumen agregado y consistente de estado operativo y validación de seguridad para su visualización en frontend.

Validación del sprint:

- Tests de integración del endpoint de summary añadidos y en verde.
- Colección Postman actualizada con request de dashboard summary.

---

## Sprint 5 - Integración PowerShell

Objetivo:

Adaptar scripts existentes para enviar eventos a BlueDeploy.

Funciones PowerShell:

- New-BlueDeployDeployment
- Send-BlueDeployStep
- Send-BlueDeployCheck
- Complete-BlueDeployDeployment

---

## Sprint 6 - Dashboard Frontend con Roles (Práctica 1 - Cierre)

Estado: COMPLETADO

Objetivo:

Rediseñar el frontend con Tailwind CSS, sistema de autenticación mock y vistas diferenciadas por rol (Administrador / Técnico).

### Tareas completadas:

- Instalación y configuración de Tailwind CSS v4 + lucide-react.
- Capa de datos mock (JSON estáticos): 2 usuarios técnicos + 7 despliegues realistas.
- AuthContext con persistencia en localStorage.
- Rutas protegidas por rol (ProtectedRoute).
- Layout compartido: Sidebar + Header responsivos con navegación por rol.
- Componentes reutilizables: Badge, StatCard, DeploymentTable, DeploymentDetail (panel lateral), CreateTechnicianModal.
- Vista Administrador:
  - Dashboard con métricas globales (6 StatCards) + gráfico de barras por técnico.
  - Listado completo de despliegues con filtros por estado y técnico.
  - Gestión de técnicos con modal de alta (modo demo).
- Vista Técnico:
  - Dashboard personal con resumen de propios despliegues.
  - Listado filtrable de sus despliegues.
  - Página de perfil con formulario de edición (modo demo).
- Login rediseñado con acceso rápido a los 3 usuarios demo.
- Test de integración Java (MockDataPopulationTest): 4 despliegues realistas, 2 técnicos, validación del summary API.
- Notas Obsidian actualizadas + nueva nota de historial de commits.

### Credenciales demo (Práctica 1):

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| admin | admin | Administrador |
| carlos | tecnico1 | Técnico |
| ana | tecnico2 | Técnico |

### Notas de Práctica 2:

- Reemplazar credenciales mock por JWT + Spring Security.
- Persistir usuarios en base de datos.
- Implementar lógica real de creación de técnicos.
- Añadir autenticación API Key para scripts PowerShell.
