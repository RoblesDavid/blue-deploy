# Asignación de Deployments y Filtrado por Rol

## Propósito

Documenta la evolución del modelo de datos para asociar despliegues (`Deployment`) a usuarios técnicos reales (`AppUser`), los nuevos endpoints de reasignación y consulta personalizada (`/api/deployments/my`), así como las directrices para la integración futura en el frontend de React.

---

## Cambios en el Backend

### 1. Entidad de Dominio `Deployment`
- Relación JPA añadida:
  ```java
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "technician_id")
  private AppUser technician;
  ```
- Clave foránea `FK_deployment_technician` apuntando a `app_user(id)`.

### 2. DTOs de Deployment
- **`DeploymentRequestDTO`**:
  - Nuevo campo opcional `technicianId` (Long) para asociar un técnico en el alta inicial del despliegue.
- **`DeploymentResponseDTO`**:
  - Nuevos campos `technicianId` (Long) y `technicianName` (String).

### 3. Repositorio `DeploymentRepository`
- Métodos añadidos para consulta de deployments asociados a técnicos:
  - `List<Deployment> findByTechnician(AppUser technician)`
  - `List<Deployment> findByTechnicianId(Long technicianId)`
  - `List<Deployment> findByTechnicianUsername(String username)`

### 4. Servicio `DeploymentService` y Controlador `DeploymentController`
- Creación de deployments vinculando al técnico si se proporciona `technicianId`.
- **`PUT /api/deployments/{deploymentId}/assign-technician/{technicianId}`**:
  - Reasigna el deployment al técnico especificado.
  - Protegido por Spring Security con rol obligatorio `ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`).
- **`GET /api/deployments`**:
  - Si el usuario autenticado tiene rol `ADMIN`, retorna todos los deployments.
  - Si el usuario autenticado tiene rol `TECHNICIAN`, filtra y retorna únicamente los deployments asignados a su usuario.
- **`GET /api/deployments/my`**:
  - Endpoint dedicado para que el usuario autenticado consulte sus propios despliegues.

---

## Matriz de Permisos de Deployments

| Endpoint | Método | Autenticación | Rol Permitido | Comportamiento |
|---|---|---|---|---|
| `/api/deployments` | `POST` | Opcional (PowerShell) | Todos | Crea un nuevo deployment (con técnico opcional) |
| `/api/deployments` | `GET` | Autenticado | `ADMIN`, `TECHNICIAN` | ADMIN ve todos, TECHNICIAN ve solo los suyos |
| `/api/deployments/my` | `GET` | Requerida | `ADMIN`, `TECHNICIAN` | Retorna los deployments asignados al usuario |
| `/api/deployments/{id}/assign-technician/{id}` | `PUT` | Requerida | `ADMIN` | Reasigna un deployment a otro técnico |
| `/api/deployments/{id}` | `GET` | PermitAll | Todos | Detalle de un deployment específico |

---

## Guía de Cambios Pendientes en Frontend (React)

Para integrar estas capacidades en el frontend más adelante, se deberán realizar los siguientes ajustes:

1. **Servicio API de Deployments (`frontend/src/services/deploymentService.js` o `api.js`)**:
   - Agregar método `assignTechnician(deploymentId, technicianId)` que invoque `PUT /api/deployments/${deploymentId}/assign-technician/${technicianId}` con cabecera `Authorization: Bearer <token>`.
   - Asegurar que las llamadas a `GET /api/deployments` y `GET /api/deployments/my` incluyan el JWT del usuario autenticado almacenado en `AuthContext`.

2. **Componente `AllDeployments.jsx` (Vista de Administrador)**:
   - Añadir selector / modal de asignación de técnico utilizando la lista de técnicos obtenida de `GET /api/users/technicians`.
   - Mostrar la columna "Técnico asignado" (`technicianName`) en `DeploymentTable`.
   - Incorporar botón/acción para invocar `assignTechnician`.

3. **Componente `MyDeployments.jsx` (Vista de Técnico)**:
   - Conectar la tabla al endpoint `GET /api/deployments` (que ya filtra automáticamente) o a `GET /api/deployments/my`.
   - Ocultar botones de reasignación ya que los técnicos no tienen privilegios administrativos.

4. **Componente `DeploymentDetail.jsx` (Panel Lateral)**:
   - Mostrar la tarjeta con la información del técnico responsable (`technicianName`, iniciales/avatar si aplica).
   - Para administradores, permitir modificar el técnico asignado directamente desde el panel de detalles.

---

## Verificación de Tests de Integración

- Suite de pruebas: `DeploymentApiIntegrationTest` (12 tests pasando):
  - `createDeployment_withAssignedTechnician_shouldReturnTechnicianDetails` (200 OK y DTO completo con técnico).
  - `getDeployments_asAdmin_shouldReturnAllDeployments` (ADMIN recibe todos los registros).
  - `getDeployments_asTechnician_shouldReturnOnlyOwnDeployments` (TECHNICIAN recibe solo sus registros asignados).
  - `getMyDeployments_asTechnician_shouldReturnOwnDeployments` (Acceso a `/api/deployments/my`).
  - `assignTechnician_asAdmin_shouldUpdateTechnician` (ADMIN reasigna técnico exitosamente).
  - `assignTechnician_asTechnician_shouldReturnForbidden` (403 Forbidden cuando un técnico intenta reasignar).
  - `assignTechnician_unauthenticated_shouldReturnUnauthorized` (401 Unauthorized sin token).
