## Objetivo

Este documento describe la arquitectura del frontend conectada al backend real de Spring Boot para la **Práctica 2**, implementando autenticación JWT, control de acceso basado en roles (`ADMIN` y `TECHNICIAN`), gestión dinámica de usuarios y despliegues, exportación de informes PDF y diagnóstico de incidencias con IA.

---

## Tecnologías

| Paquete | Versión | Uso |
|---------|---------|-----|
| React | 19.2.5 | Framework UI reactivo |
| Vite | 8.0.10 | Build tool y servidor de desarrollo |
| React Router DOM | 7.14.2 | Enrutamiento SPA basado en roles |
| Tailwind CSS | 4.3.0 | Sistema de diseño y estilos |
| Lucide React | 1.17.0 | Iconografía vectorial |
| Chart.js + React-Chartjs-2 | 4.5.1 | Visualización de gráficos estadísticos |

---

## Evolución Práctica 2 — Autenticación real y APIs

En la Práctica 2, el frontend elimina la dependencia de mocks y se conecta de forma directa y reactiva al backend de Spring Boot.

### Cambios implementados:
- **Login conectado a `POST /api/auth/login`**:
  - Emisión de token JWT con tiempo de expiración y claims (`role`, `userId`, `username`, `name`).
  - Persistencia segura del token en `localStorage` (`bluedeploy_token`).
- **`apiClient` centralizado** (`frontend/src/services/apiClient.js`):
  - Inyección automática de cabecera `Authorization: Bearer <token>`.
  - Detección y manejo de respuestas `401 Unauthorized` con deslogueo reactivo.
  - Soporte de descarga binaria de streams PDF con `downloadBlob`.
- **Servicios API modulares**:
  - `authApi.js`: Login y verificación de sesión.
  - `usersApi.js`: `GET /api/users/me`, `PUT /api/users/me`, `GET /api/users/technicians`, `POST /api/users/technicians`.
  - `deploymentsApi.js`: `GET /api/deployments`, `GET /api/deployments/my`, `GET /api/deployments/{id}`, `PUT /assign-technician/{id}`, `GET /report.pdf`, `POST /ai-summary`.
  - `dashboardApi.js`: `GET /api/dashboard/summary`.
  - `auditApi.js`: `GET /api/audit`.

---

## Arquitectura de Rutas y Control de Acceso

```text
/login                   → Login (público / conectado a API real)
/                        → RoleRedirect (→ /admin o /technician según rol)
/admin                   → Layout con Sidebar admin
  /admin                 → AdminDashboard (métricas globales y analítica)
  /admin/deployments     → AllDeployments (inventario completo + reasignación)
  /admin/technicians     → TechnicianManagement (CRUD técnicos persistido)
/technician              → Layout con Sidebar técnico
  /technician            → TechnicianDashboard (métricas de despliegues asignados)
  /technician/deployments → MyDeployments (filtrado estricto por técnico autenticado)
  /technician/profile    → MyProfile (actualización de perfil y credenciales)
```

---

## Funcionalidades Avanzadas de Despliegue

### 1. Exportación PDF de Informes
- Botón integrado en `DeploymentDetail.jsx` que consume `GET /api/deployments/{id}/report.pdf`.
- Genera documento PDF con OpenPDF conteniendo:
  - Ficha técnica y técnico responsable.
  - Tabla cronológica de fases OSDCloud.
  - Tabla de checks de seguridad y estado de cumplimiento.

### 2. Diagnóstico Asistido con Inteligencia Artificial
- Botón "Diagnóstico IA" en `DeploymentDetail.jsx` que consume `POST /api/deployments/{id}/ai-summary`.
- Muestra tarjeta de severidad (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`), resumen en lenguaje natural, causa técnica probable y lista de acciones recomendadas de mitigación.

### 3. Centro de Notificaciones Interactivas
- Campana de notificaciones interactiva en la cabecera (`Header.jsx`) con badge en tiempo real de incidencias pendientes (`NON_COMPLIANT`, `FAILED`, `COMPLETED_WITH_WARNINGS`) y equipos pendientes de asignación.
- Soporte para marcar como leídas y navegación directa al detalle del equipo.

### 4. Flujo de Remediación de Comprobaciones y Autoasignación
- Botón "Corregir / Remediado" en checks fallidos/pendientes para que técnicos y administradores puedan reflejar correcciones manuales con nota explicativa (`PUT /api/deployments/{id}/checks/{checkId}/resolve`).
- Recálculo automático del estado de cumplimiento y actualización inmediata en el dashboard.
- Botón "Asignarme" para que técnicos puedan asumir despliegues no asignados creados por scripts desatendidos (`PUT /api/deployments/{id}/claim`).

### 5. Modificación Manual de Estados y Fases
- Botón y formulario interactivo "Cambiar Estado Manual" en `DeploymentDetail.jsx` (`PUT /api/deployments/{id}/status`).
- Permite a técnicos y administradores forzar o corregir estados del despliegue (`IN_PROGRESS`, `COMPLETED`, `COMPLETED_WITH_WARNINGS`, `FAILED`, etc.), niveles de compliance (`PENDING`, `COMPLIANT`, `NON_COMPLIANT`) y la fase actual del sistema.
- Incluye campo para notas operativas con registro automático en el módulo de auditoría (`AuditAction.DEPLOYMENT_STATUS_CHANGED`).
