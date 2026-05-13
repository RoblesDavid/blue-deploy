
Este archivo recogerá evidencias para el informe final.

## Evidencia - Sprint 1: API de Deployment

Fecha: 2026-05-11  
Rama: feature/deployment-model

### Objetivo

Validar que BlueDeploy puede registrar un nuevo despliegue OSDCloud mediante la entidad Deployment.

### Endpoints probados

- POST /api/deployments
- GET /api/deployments
- GET /api/deployments/{deploymentId}

### Prueba 1 - Crear deployment

Comando ejecutado:

```bash
curl -X POST http://localhost:8080/api/deployments
-H "Content-Type: application/json"
-d '{ "deploymentId": "DEPLOY-PC001-20260510", "hostname": "PC001", "deviceModel": "Dell Latitude 5440", "domainName": "corp.local" }'
```

Resultado obtenido:

```json

json { "id": 1, "deploymentId": "DEPLOY-PC001-20260510", "hostname": "PC001", "deviceModel": "Dell Latitude 5440", "domainName": "corp.local", "currentStep": "DEPLOYMENT_CREATED", "deploymentStatus": "IN_PROGRESS", "complianceStatus": "PENDING", "startedAt": "2026-05-11T05:30:50.492128185", "finishedAt": null, "lastSeenAt": "2026-05-11T05:30:50.492128185" }
```

### Prueba 2 - Listar deployments

Comando ejecutado:

```bash
curl http://localhost:8080/api/deployments
```

Resultado obtenido:

```json
[{"id":1,"deploymentId":"DEPLOY-PC001-20260510","hostname":"PC001","deviceModel":"Dell Latitude 5440","domainName":"corp.local","currentStep":"DEPLOYMENT_CREATED","deploymentStatus":"IN_PROGRESS","complianceStatus":"PENDING","startedAt":"2026-05-11T05:30:50.492128","finishedAt":null,"lastSeenAt":"2026-05-11T05:30:50.492128"}]
```


### Prueba 3 - Consultar deployment por deploymentId

Comando ejecutado:

```bash
curl http://localhost:8080/api/deployments/DEPLOY-PC001-20260510
```

Resultado:

```json
json{ "id": 1, "deploymentId": "DEPLOY-PC001-20260510", "hostname": "PC001", "deviceModel": "Dell Latitude 5440", "domainName": "corp.local", "currentStep": "DEPLOYMENT_CREATED", "deploymentStatus": "IN_PROGRESS", "complianceStatus": "PENDING", "startedAt": "2026-05-11T05:30:50.492128", "finishedAt": null, "lastSeenAt": "2026-05-11T05:30:50.492128" }
```

### Resultado

La prueba ha sido satisfactoria.

BlueDeploy ya puede registrar el inicio de un despliegue Windows corporativo, almacenar su estado inicial y consultarlo mediante API REST.

### Captura

![[Pasted image 20260511210047.png]]

## Evidencia 2 - Sprint 2: Registro de fases DeploymentStep

Fecha: 2026-05-11  
Rama: feature/deployment-steps

### Objetivo

Validar que BlueDeploy puede registrar fases del despliegue OSDCloud asociadas a un Deployment existente.

### Endpoints probados

- POST /api/deployments/{deploymentId}/steps
- GET /api/deployments/{deploymentId}/steps
- GET /api/deployments/{deploymentId}

### Prueba 1 - Registrar fase DRIVER_INSTALLATION

Comando ejecutado:

```bash
curl -X POST http://localhost:8080/api/deployments/DEPLOY-PC002-20260511/steps \
  -H "Content-Type: application/json" \
  -d '{
    "stepName": "DRIVER_INSTALLATION",
    "stepStatus": "SUCCESS",
    "message": "Drivers installed successfully",
    "errorCode": null,
    "rawOutput": "Detected model: Dell Latitude 5440. Driver package installed."
  }'
```
Resultado esperado:

```json
{
  "deploymentId": "DEPLOY-PC002-20260511",
  "stepName": "DRIVER_INSTALLATION",
  "stepStatus": "SUCCESS",
  "message": "Drivers installed successfully"
}

```


![[Pasted image 20260513210231.png]]
### Prueba 2 - Registrar fase DOMAIN_JOIN

Comando ejecutado:

```bash
curl -X POST http://localhost:8080/api/deployments/DEPLOY-PC002-20260511/steps \
  -H "Content-Type: application/json" \
  -d '{
    "stepName": "DOMAIN_JOIN",
    "stepStatus": "SUCCESS",
    "message": "Device joined to corporate domain successfully",
    "errorCode": null,
    "rawOutput": "Domain join completed."
  }'
   ```
Resultado esperado:
El Deployment debe actualizarse a:
```json
{
"currentStep": "DOMAIN_JOIN",
"deploymentStatus": "DOMAIN_JOINED"
}
```

### Prueba 3 - Registrar fase GPUPDATE
Comando ejecutado:

```bash
curl -X POST http://localhost:8080/api/deployments/DEPLOY-PC002-20260511/steps \
  -H "Content-Type: application/json" \
  -d '{
    "stepName": "GPUPDATE",
    "stepStatus": "SUCCESS",
    "message": "Group policies applied successfully",
    "errorCode": null,
    "rawOutput": "gpupdate /force completed."
  }'
  ```
Resultado esperado:
El Deployment debe actualizarse a:
```json
{
  "currentStep": "GPUPDATE",
  "deploymentStatus": "POST_DOMAIN_PENDING"
}
``` 
### Prueba 4 - Consultar fases registradas
Comando ejecutado:

```bash
curl http://localhost:8080/api/deployments/DEPLOY-PC002-20260511/steps
``` 

```json
[
  {
    "stepName": "DRIVER_INSTALLATION",
    "stepStatus": "SUCCESS"
  },
  {
    "stepName": "DOMAIN_JOIN",
    "stepStatus": "SUCCESS"
  },
  {
    "stepName": "GPUPDATE",
    "stepStatus": "SUCCESS"
  }
]
```

![[Pasted image 20260513210359.png]]

### Prueba 5 - Registrar fase fallida

Comando ejecutado:

```bash
curl -X POST http://localhost:8080/api/deployments/DEPLOY-PC003-20260511/steps \
  -H "Content-Type: application/json" \
  -d '{
    "stepName": "RADIUS_8021X_CONFIGURATION",
    "stepStatus": "FAILED",
    "message": "802.1X configuration failed",
    "errorCode": "RADIUS_PROFILE_ERROR",
    "rawOutput": "Unable to apply network profile."
  }'
  ```

Resultado esperado:
El Deployment debe actualizarse a:

```json 
{
  "currentStep": "RADIUS_8021X_CONFIGURATION",
  "deploymentStatus": "FAILED"
}
```

## Evidencia 3 - MariaDB en Docker

Pendiente de añadir captura.

## Evidencia 4 - Dashboard React

Pendiente de añadir captura.

## Evidencia 5 - Registro de deployment

Pendiente de añadir captura.

## Evidencia 6 - Registro de fases PowerShell

Pendiente de añadir captura.

## Evidencia 7 - Validación post-dominio

Pendiente de añadir captura.

## Evidencia 8 - Despliegue en Hetzner

Pendiente de añadir captura.

## Evidencia 9 - Roadmap Práctica 2

Pendiente de añadir captura.