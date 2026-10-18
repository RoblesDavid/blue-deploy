# Gestión de Usuarios y Roles Backend (Sprint 8 — Práctica 2)

## Propósito

Documenta la arquitectura de persistencia, modelo de entidades, DTOs y endpoints para sustituir los mocks del frontend por usuarios almacenados en base de datos, sentando las bases para el posterior sprint de autenticación con Spring Security y JWT.

---

## Modelo de Dominio

### Entidad `AppUser` (`app_user`)
- `id` (`Long`, PK Auto-incremental)
- `username` (`VARCHAR(255)`, `UNIQUE`, `NOT NULL`)
- `password` (`VARCHAR(255)`, `NOT NULL`)
- `name` (`VARCHAR(255)`, `NOT NULL`)
- `initials` (`VARCHAR(255)`) — autocalculado a partir del nombre si no se proporciona.
- `email` (`VARCHAR(255)`, `UNIQUE`, `NOT NULL`)
- `role` (`VARCHAR(255)`, `NOT NULL`) — `ADMIN` o `TECHNICIAN`.
- `department` (`VARCHAR(255)`)
- `active` (`BOOLEAN`, `NOT NULL`, por defecto `true`) — para borrado lógico de técnicos.
- `createdAt` (`TIMESTAMP`, `NOT NULL`)
- `updatedAt` (`TIMESTAMP`)

### Enum `UserRole`
- `ADMIN`: Administrador con capacidad de crear técnicos, listar técnicos y gestionar configuración global.
- `TECHNICIAN`: Técnico responsable de despliegues OSDCloud y edición de su propio perfil.

---

## Endpoints REST (`/api/users`)

| Método | Endpoint | Descripción | Body Entrada | Respuesta |
|---|---|---|---|---|
| `GET` | `/api/users/me` | Obtiene el perfil del usuario actual (soporta headers `X-User-Id` o `X-Username` previo a JWT) | - | `UserResponseDTO` (200 OK) |
| `PUT` | `/api/users/me` | Actualiza los datos del usuario actual | `UpdateProfileRequestDTO` | `UserResponseDTO` (200 OK) |
| `GET` | `/api/users/technicians` | Lista los técnicos registrados | - | `List<UserResponseDTO>` (200 OK) |
| `POST` | `/api/users/technicians` | Alta de nuevo técnico por Administrador | `CreateTechnicianRequestDTO` | `UserResponseDTO` (201 Created) |
| `PUT` | `/api/users/technicians/{id}` | Edición de un técnico existente | `UpdateProfileRequestDTO` | `UserResponseDTO` (200 OK) |
| `PATCH` | `/api/users/technicians/{id}/disable` | Desactivación (borrado lógico) de técnico | - | `UserResponseDTO` (200 OK) |

---

## Reglas de Negocio Implementadas

1. **Unicidad:** No se permite duplicar `username` ni `email`.
2. **Cálculo de iniciales:** Si no se proporcionan iniciales en el alta o edición, el sistema toma la inicial del primer nombre y primer apellido.
3. **Baja lógica:** Los técnicos no se eliminan físicamente para mantener la trazabilidad de los despliegues históricos.
4. **Seguridad de contraseñas:** El DTO de salida `UserResponseDTO` nunca expone el campo `password`.

---

## Verificación

- Suite de integración H2: `UserApiIntegrationTest` (8 tests).
- Ejecución: `mvn test -Dtest=UserApiIntegrationTest`.
