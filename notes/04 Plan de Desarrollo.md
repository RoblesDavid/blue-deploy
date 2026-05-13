
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

Endpoint:

- POST /api/deployments/{deploymentId}/steps
- GET /api/deployments/{deploymentId}/steps
- Estados automáticos iniciales:

- DOMAIN_JOIN + SUCCESS → DOMAIN_JOINED
- GPUPDATE + SUCCESS → POST_DOMAIN_PENDING
- FINAL_SECURITY_VALIDATION + SUCCESS → COMPLETED
- Cualquier fase + FAILED → FAILED

Resultado:

BlueDeploy puede registrar el progreso de un despliegue OSDCloud por fases y consultar el histórico de pasos de cada equipo.

---

## Sprint 3 - Checks de seguridad

Objetivo:

Registrar validaciones post-dominio.

Tareas:

- Crear SecurityCheck
- Crear CheckStatus
- Crear Severity
- Crear SecurityCheckRepository
- Crear SecurityCheckService
- Crear SecurityCheckController

Endpoint:

- POST /api/deployments/{deploymentId}/checks

Checks iniciales:

- FIREWALL_ENABLED
- WSUS_CONFIGURED
- CROWDSTRIKE_VALIDATED
- MICROCLAUDIA_VALIDATED
- LOCAL_ADMIN_VALIDATED

---

## Sprint 4 - Dashboard

Objetivo:

Mostrar información relevante en el frontend.

Métricas:

- Total de despliegues
- Despliegues completados
- Despliegues fallidos
- Pendientes post-dominio
- Pendientes CrowdStrike
- Pendientes MicroClaudia
- Últimos equipos desplegados

---

## Sprint 5 - Integración PowerShell

Objetivo:

Adaptar scripts existentes para enviar eventos a BlueDeploy.

Funciones PowerShell:

- New-BlueDeployDeployment
- Send-BlueDeployStep
- Send-BlueDeployCheck
- Complete-BlueDeployDeployment