
## Descripción

BlueDeploy es una plataforma Blue Team para monitorizar despliegues Windows corporativos realizados con OSDCloud.

El sistema centraliza los eventos generados por scripts PowerShell durante el despliegue, registra el estado de cada fase y valida controles de seguridad post-dominio.

## Problema

En un despliegue automatizado con OSDCloud, el proceso puede parecer correcto en pantalla, pero no siempre existe una trazabilidad centralizada de:

- Instalación de drivers
- Eliminación de bloatware
- Configuración 802.1X / RADIUS
- Unión a dominio
- Ejecución de gpupdate
- Unión a WSUS
- Instalación de CrowdStrike
- Instalación de MicroClaudia
- Eliminación o control de usuarios administrativos
- Errores durante scripts PowerShell
- Estado final de cumplimiento de seguridad

## Solución

BlueDeploy proporciona un dashboard web que permite consultar:

- Últimos equipos desplegados
- Estado de cada despliegue
- Fases correctas, pendientes o fallidas
- Equipos unidos correctamente al dominio
- Equipos pendientes de validación post-dominio
- Equipos sin CrowdStrike validado
- Equipos sin MicroClaudia validada
- Alertas Blue Team
- Estado de cumplimiento de hardening

## Enfoque Blue Team

El proyecto se alinea con defensa y monitorización porque permite detectar equipos desplegados de forma incompleta o insegura.

BlueDeploy no sustituye a un EDR, SIEM o herramienta de inventario, sino que actúa como capa de trazabilidad y validación de seguridad del proceso de despliegue.