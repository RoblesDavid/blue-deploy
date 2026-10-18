# Módulos de Auditoría y Exportación de Informes PDF

## Módulo de Auditoría (AuditLog)

### Objetivo
Garantizar la trazabilidad y rendición de cuentas en eventos críticos del sistema BlueDeploy (seguridad y operativa de despliegue).

### Acciones Auditadas (`AuditAction`):
- `LOGIN_SUCCESS`: Inicios de sesión correctos con indicación de rol.
- `LOGIN_FAILED`: Intentos fallidos por credenciales o cuentas desactivadas.
- `TECHNICIAN_CREATED`: Registro de nuevos técnicos por administradores.
- `PROFILE_UPDATED`: Modificaciones de datos de contacto o contraseñas.
- `DEPLOYMENT_ASSIGNED`: Reasignación operativa de despliegues.
- `SCRIPT_EVENT_RECEIVED`: Ingesta de fases y verificaciones desde scripts.
- `INVALID_API_KEY`: Detección de peticiones con clave API no autorizada.
- `DEPLOYMENT_STATUS_CHANGED`: Transiciones de estado global.
- `COMPLIANCE_STATUS_CHANGED`: Cambios en conformidad de seguridad.

### Endpoints de Auditoría (Rol ADMIN):
- `GET /api/audit`: Listado cronológico descendente de todos los eventos.
- `GET /api/audit/deployments/{id}`: Historial de auditoría para un despliegue.
- `GET /api/audit/users/{id}`: Acciones asociadas a un usuario concreto.

---

## Módulo de Exportación de Informes PDF (OpenPDF)

### Objetivo
Generar ficheros PDF ejecutivos y técnicos para acreditación de cumplimiento en auditorías de seguridad y entrega de equipos.

### Contenido del Documento:
1. **Cabecera Institucional**: Título BlueDeploy, fecha de generación y clasificación.
2. **Ficha de Despliegue**: Deployment ID, Hostname, Modelo, Dominio, Técnico asignado, Estado Global y Estado de Compliance.
3. **Línea Temporal OSDCloud**: Tabla con número de fase, nombre, estado, código de error y marcas de tiempo.
4. **Matriz de Checks de Seguridad**: Verificaciones de CrowdStrike, MicroClaudia, BitLocker, LAPS y configuraciones críticas con valores esperados vs obtenidos.
5. **Pie de Página**: Declaración de confidencialidad y conformidad técnica.

### Endpoint y Permisos:
- `GET /api/deployments/{deploymentId}/report.pdf`
- **Permisos**:
  - `ADMIN`: puede descargar cualquier informe.
  - `TECHNICIAN`: solo puede descargar los informes de los despliegues asignados a su cuenta.
