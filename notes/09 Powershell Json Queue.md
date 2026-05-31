# PowerShell JSON Queue

## Objetivo

Durante el despliegue real OSDCloud, los scripts generan archivos JSON locales en lugar de enviar directamente a la API.

Esto permite:

- Evitar problemas de firewall.
- Conservar evidencias.
- Reintentar envíos.
- Simular integración real desde el equipo del desarrollador.

## Carpeta de cola

```text
C:\OSDCloud\BlueDeploy\Queue
```

## Tipos de eventos
- DEPLOYMENT
- DEPLOYMENT_STEP
- SECURITY_CHECK

## Flujo
``` text
Script OSDCloud
    ↓
Genera JSON
    ↓
Copia manual de JSON
    ↓
Send-BlueDeployQueue.ps1
    ↓
BlueDeploy API
```
```text
Durante la Práctica 1, los scripts reales de entorno corporativo quedan excluidos del repositorio mediante .gitignore por contener información sensible. Para la entrega se incluyen scripts auxiliares y muestras anonimizadas.
```
