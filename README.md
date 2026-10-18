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

- Dashboard React + Vite + Tailwind CSS v4.
- Sistema de autenticación mock con dos roles: Administrador y Técnico.
- Vista de administrador: métricas globales, gráfico por técnico, gestión de técnicos.
- Vista de técnico: dashboard personal, despliegues propios, perfil editable.
- Panel de detalle de despliegues con fases y verificaciones de seguridad.
- Datos mock estáticos (7 despliegues, 2 técnicos) para demo sin backend.

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
- [x] Modelo Deployment.
- [x] Registro de fases DeploymentStep.
- [x] Registro de checks post-dominio (SecurityCheck).
- [x] Dashboard Summary API (GET /api/dashboard/summary).
- [x] Integración PowerShell (cola JSON local).
- [x] Dashboard React con roles (Administrador / Técnico).
- [x] Autenticación mock con JWT preparado para Práctica 2.
- [x] Tests de integración (25+ tests, todos en verde).
- [x] Documentación técnica en Obsidian (12 notas).
- [ ] Despliegue en Hetzner (pendiente).

### Práctica 2

- [x] API Key para scripts PowerShell e ingesta resiliente (`X-BlueDeploy-Api-Key`).
- [x] Autenticación JWT con roles (ADMIN y TECHNICIAN) y protección RBAC en Spring Security.
- [x] Diagnóstico inteligente de errores asistido por IA (`AiSummary`).
- [x] Exportación de informes oficiales de auditoría y conformidad en PDF (`OpenPDF`).
- [x] Registro inmutable de eventos de seguridad y auditoría (`AuditLog`).
- [x] Landing Page introductoria con navegación, acceso y contacto de David Robles Gaona.
- [x] Despliegue de producción contenerizado en Hetzner con Nginx y MariaDB 11.
- [x] Suite de 95 tests de integración automatizados con Maven (100% superados).

---

## Documentación

La documentación técnica del proyecto se encuentra en la carpeta:

```text
notes/
```

Estas notas están pensadas para ser consultadas desde Obsidian y servir como base para el informe final en PDF.

---

Proyecto desarrollado por **David Robles Gaona** (Tel: 629096538 · [LinkedIn](https://www.linkedin.com/in/david-robles-gaona)) como parte del Máster Online en Ciberseguridad & Inteligencia Artificial por Evolve Academy.

