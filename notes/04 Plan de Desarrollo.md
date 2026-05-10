
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

Tareas:

- Crear Deployment
- Crear DeploymentStatus
- Crear ComplianceStatus
- Crear DeploymentRepository
- Crear DeploymentRequestDTO
- Crear DeploymentResponseDTO
- Crear DeploymentService
- Crear DeploymentController

Endpoints:

- POST /api/deployments
- GET /api/deployments
- GET /api/deployments/{id}

---

## Sprint 2 - Registro de fases

Objetivo:

Registrar cada fase del despliegue enviada desde PowerShell.

Tareas:

- Crear DeploymentStep
- Crear StepStatus
- Crear DeploymentStepRepository
- Crear DeploymentStepRequestDTO
- Crear DeploymentStepResponseDTO
- Crear DeploymentStepService
- Crear endpoint para registrar fases

Endpoint:

- POST /api/deployments/{deploymentId}/steps

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