# BlueDeploy

BlueDeploy es una plataforma Blue Team para monitorizar despliegues Windows corporativos realizados con OSDCloud.

El sistema registra fases del despliegue ejecutadas mediante PowerShell, centraliza evidencias en una API Spring Boot y muestra en un dashboard web el estado de los equipos, errores detectados y validaciones de seguridad post-dominio.

---

## Objetivo

El objetivo de BlueDeploy es proporcionar trazabilidad y validación de seguridad durante el ciclo de despliegue de equipos Windows corporativos.

Permite responder preguntas como:

- ¿Qué equipos se han desplegado recientemente?
- ¿Qué equipos se han unido correctamente al dominio?
- ¿En qué fase ha fallado un despliegue?
- ¿Qué equipos están pendientes de validación post-dominio?
- ¿Qué equipos no tienen CrowdStrike validado?
- ¿Qué equipos no tienen MicroClaudia validada?
- ¿Qué equipos presentan incumplimientos básicos de hardening?

---

## Flujo general

text OSDCloud / PowerShell ? Eventos JSON por fase ? API REST Spring Boot ? MariaDB ? Dashboard React


---

## Estado actual

### Backend

- API REST en Spring Boot.
- Persistencia en MariaDB.
- Recepción inicial de checks de seguridad.
- Sistema básico de alertas.
- Arquitectura Controller → Service → DTO → Entity.

### Frontend

- Dashboard React + Vite.
- Visualización de estados.
- Métricas iniciales.
- Auto-refresh de datos.

### Próxima evolución

El proyecto se está reestructurando para representar el despliegue como un proceso por fases:

- Deployment.
- DeploymentStep.
- SecurityCheck.
- Alert.
- DashboardSummary.

---

## Enfoque Blue Team

BlueDeploy se orienta a defensa y monitorización mediante:

- Trazabilidad de despliegues.
- Validación de hardening.
- Detección de despliegues incompletos.
- Alertas de seguridad.
- Validación post-dominio.
- Evidencias para auditoría.

---

## Fases previstas del despliegue

- HOSTNAME_CAPTURED
- WINDOWS_INSTALLATION
- BLOATWARE_REMOVAL
- DRIVER_MODEL_DETECTED
- DRIVER_INSTALLATION
- RADIUS_8021X_CONFIGURATION
- DOMAIN_JOIN
- GPUPDATE
- WSUS_CONFIGURATION
- APPLICATION_SELECTION
- OSDCLOUD_CLEANUP
- CROWDSTRIKE_VALIDATION
- MICROCLAUDIA_VALIDATION
- LOCAL_ADMIN_VALIDATION
- FINAL_SECURITY_VALIDATION

---

## Tecnologías

### Backend

- Java 21
- Spring Boot
- Maven
- MariaDB
- Docker

### Frontend

- React
- Vite
- React Router

### Automatización

- PowerShell
- OSDCloud

---

## Ejecución local


bash docker compose build docker compose up



---

## Roadmap

### Práctica 1

- [x] Backend Spring Boot inicial.
- [x] MariaDB con Docker.
- [x] Dashboard React inicial.
- [x] Recepción de checks básicos.
- [ ] Modelo Deployment.
- [ ] Registro de fases DeploymentStep.
- [ ] Registro de checks post-dominio.
- [ ] Dashboard de despliegues.
- [ ] Documentación profesional.
- [ ] Despliegue en Hetzner.

### Práctica 2

- [ ] API Key para scripts PowerShell.
- [ ] Autenticación JWT para dashboard.
- [ ] Integración IA para análisis de errores.
- [ ] Exportación de informes PDF.
- [ ] Integración con CrowdStrike/Altiris.
- [ ] Sistema de reglas configurable.
- [ ] Alertas por Teams o email.
- [ ] Hardening del despliegue en Hetzner.

---

## Documentación

La documentación técnica del proyecto se encuentra en la carpeta:

```text
 notes/
 
 ```  
Estas notas están pensadas para ser consultadas desde Obsidian y servir como base para el informe final en PDF.

---

Proyecto desarrollado como parte de formación en Ciberseguridad e Inteligencia Artificial.

