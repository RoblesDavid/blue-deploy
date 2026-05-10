## Objetivo general

Evolucionar BlueDeploy desde una herramienta de monitorización de despliegues OSDCloud hacia una plataforma cliente/servidor con validación avanzada de seguridad y automatización asistida.

## Mejoras planificadas

| Mejora | Descripción | Prioridad | Estimación |
|---|---|---:|---:|
| API Key para agentes | Proteger las llamadas de PowerShell a la API | Alta | 1 semana |
| JWT para dashboard | Autenticación real para usuarios del panel | Alta | 1 semana |
| Integración IA | Resumir errores y proponer mitigaciones | Alta | 1-2 semanas |
| Exportación PDF | Generar informe por equipo | Media | 1 semana |
| Cliente/Servidor OSDCloud | Evolucionar desde USB a despliegue centralizado | Alta | 3-4 semanas |
| Integración CrowdStrike API | Validar EDR desde fuente corporativa | Media | 2 semanas |
| Integración Altiris | Validar instalación de software corporativo | Media | 2 semanas |
| Alertas Teams/Email | Notificar despliegues fallidos o inseguros | Media | 1 semana |
| Reglas configurables | Definir controles de compliance desde panel | Media | 2 semanas |
| Hardening Docker/Hetzner | Mejorar seguridad del despliegue | Alta | 1 semana |

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