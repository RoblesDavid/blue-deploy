## Objetivo general

Evolucionar BlueDeploy desde una herramienta de monitorización de despliegues OSDCloud hacia una plataforma cliente/servidor con validación avanzada de seguridad y automatización asistida.

## Mejoras planificadas

| Mejora | Descripción | Prioridad | Estimación | Estado |
|---|---|---:|---:|:---:|
| Persistencia de usuarios | Modelo AppUser, roles y endpoints CRUD de técnicos | Alta | Sprint 8 | Completado |
| JWT para dashboard | Autenticación y autorización Spring Security + JWT | Alta | 1 semana | Pendiente |
| API Key para agentes | Proteger las llamadas de PowerShell a la API | Alta | 1 semana | Pendiente |
| Integración IA | Resumir errores y proponer mitigaciones | Alta | 1-2 semanas | Pendiente |
| Exportación PDF | Generar informe por equipo | Media | 1 semana | Pendiente |
| Cliente/Servidor OSDCloud | Evolucionar desde USB a despliegue centralizado | Alta | 3-4 semanas | Pendiente |
| Integración CrowdStrike API | Validar EDR desde fuente corporativa | Media | 2 semanas | Pendiente |
| Integración Altiris | Validar instalación de software corporativo | Media | 2 semanas | Pendiente |
| Alertas Teams/Email | Notificar despliegues fallidos o inseguros | Media | 1 semana | Pendiente |
| Reglas configurables | Definir controles de compliance desde panel | Media | 2 semanas | Pendiente |
| Hardening Docker/Hetzner | Mejorar seguridad del despliegue | Alta | 1 semana | Pendiente |

## Mejoras de seguridad

- API Key para scripts.
- JWT para usuarios.
- HTTPS en Hetzner.
- Variables de entorno protegidas.
- CORS restringido.
- Logs sin secretos.
- Auditoría de acciones.

## Mejoras de escalabilidad

- Paginación en endpoints.
- Índices en base de datos.
- Separación frontend/backend.
- Reverse proxy.
- Backups de MariaDB.