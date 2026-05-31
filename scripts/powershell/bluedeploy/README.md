# BlueDeploy PowerShell JSON Queue

## Objetivo

Este módulo permite generar evidencias JSON locales durante despliegues OSDCloud y enviarlas posteriormente a la API BlueDeploy.

Durante la fase actual del proyecto, el envío directo desde el equipo desplegado puede estar limitado por cortafuegos. Por eso se utiliza un modelo de cola local.

## Cola local en el equipo desplegado

```text 
C:\tmp\BlueDeploy\Queue
```


## Tipos de evento

- DEPLOYMENT
- DEPLOYMENT_STEP
- SECURITY_CHECK

## Scripts

```text 
BlueDeployJson.ps1 Send-BlueDeployQueue.ps1
```

## Flujo

```text
 Script OSDCloud
        ↓
 Genera JSON en C:\tmp\BlueDeploy\Queue 
        ↓ 
 Se copian los JSON al equipo del operador 
        ↓ 
 Send-BlueDeployQueue.ps1 envía los eventos a la API 
        ↓ 
 BlueDeploy actualiza Deployment, Steps, Checks y Dashboard
```

## Enviar cola

```powershell
pwsh ./scripts/powershell/bluedeploy/Send-BlueDeployQueue.ps1 -QueuePath "./scripts/powershell/bluedeploy/examples/sample-queue" -ApiUrl "http://localhost:8080"
```


## Seguridad

Los scripts reales OSDCloud que contienen credenciales se mantienen fuera de Git mediante `.gitignore`.

En fases posteriores se implementará:

- API Key para agentes.
- HTTPS.
- Protección de credenciales.
- Cifrado de secretos.

---
