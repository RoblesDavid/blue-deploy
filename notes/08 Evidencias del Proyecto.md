
Este archivo recogerá evidencias para el informe final.

## Evidencia 1 - Creación del proyecto

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

## Evidencia 2 - Backend Spring Boot funcionando

Pendiente de añadir captura.

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