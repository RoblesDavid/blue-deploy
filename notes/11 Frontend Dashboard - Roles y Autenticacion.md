## Objetivo

Este documento describe la arquitectura del frontend a partir del Sprint 6 (Práctica 1), que introduce un sistema de roles, autenticación mock y vistas diferenciadas para Administrador y Técnico.

---

## Tecnologías

| Paquete | Versión | Uso |
|---------|---------|-----|
| React | 19.2.5 | Framework UI |
| Vite | 8.0.10 | Build tool |
| React Router DOM | 7.14.2 | Enrutamiento |
| Tailwind CSS | 4.3.0 | Estilos utilitarios |
| @tailwindcss/vite | – | Plugin Vite para Tailwind v4 |
| lucide-react | – | Iconografía SVG |
| Chart.js + react-chartjs-2 | 4.5.1 | Gráficos de barras |

---

## Modo Mock (Práctica 1)

El sistema de autenticación y los datos de despliegues son completamente estáticos (no se conectan al backend).

- Usuarios: `frontend/src/data/mockUsers.js`
- Despliegues: `frontend/src/data/mockDeployments.js`
- Credenciales almacenadas en `localStorage` bajo clave `bluedeploy_user`

### Usuarios disponibles

| username | password | role |
|----------|----------|------|
| admin | admin | ADMIN |
| carlos | tecnico1 | TECHNICIAN |
| ana | tecnico2 | TECHNICIAN |

---

## Arquitectura de rutas

```
/login                   → Login (público)
/                        → RoleRedirect (→ /admin o /technician según rol)
/admin                   → Layout con Sidebar admin
  /admin                 → AdminDashboard
  /admin/deployments     → AllDeployments
  /admin/technicians     → TechnicianManagement
/technician              → Layout con Sidebar técnico
  /technician            → TechnicianDashboard
  /technician/deployments → MyDeployments
  /technician/profile    → MyProfile
```

Rutas protegidas por `ProtectedRoute` que comprueba autenticación y rol.

---

## Flujo de autenticación (mock)

1. Usuario introduce credenciales en `Login.jsx`.
2. `AuthContext.login()` busca en `MOCK_USERS`.
3. Si coincide, guarda el usuario (sin contraseña) en `localStorage`.
4. React Router redirige a `/admin` o `/technician` según el rol.
5. En recarga de página, `AuthContext` recupera el usuario de `localStorage`.
6. `AuthProvider` envuelve toda la aplicación en `App.jsx`.

---

## Componentes principales

### Layout (`components/layout/`)

| Componente | Descripción |
|-----------|-------------|
| `Layout.jsx` | Contenedor principal: Sidebar + Header + `<Outlet />` |
| `Sidebar.jsx` | Navegación lateral adaptada al rol; logo, badges de rol, nombre de usuario, logout |
| `Header.jsx` | Barra superior con título de página dinámica e icono de notificaciones |

### Comunes (`components/common/`)

| Componente | Props | Descripción |
|-----------|-------|-------------|
| `Badge` | `status`, `label?` | Etiqueta de color según estado/severidad |
| `StatCard` | `title`, `value`, `icon`, `color`, `subtitle?` | Tarjeta de métrica con icono |

### Despliegues (`components/deployments/`)

| Componente | Props | Descripción |
|-----------|-------|-------------|
| `DeploymentTable` | `deployments[]`, `showTechnician?` | Tabla con paginación visual; abre DeploymentDetail |
| `DeploymentDetail` | `deployment`, `onClose` | Panel lateral deslizante con fases, checks y timestamps |

### Modales (`components/modals/`)

| Componente | Props | Descripción |
|-----------|-------|-------------|
| `CreateTechnicianModal` | `onClose` | Formulario de alta de técnico (demo sin persistencia) |

---

## Vistas Administrador

### AdminDashboard
- 6 tarjetas de métricas: Total, Activos, Completados, Advertencias, Fallidos, No conformes.
- Gráfico de barras apiladas (Chart.js) con desglose por técnico.
- Tarjetas resumen de técnicos activos.
- Tabla de los 5 despliegues más recientes.

### AllDeployments
- Tabla completa de todos los despliegues.
- Filtros: búsqueda libre + estado + técnico.

### TechnicianManagement
- Tarjetas por técnico con estadísticas de despliegues.
- Botón "Nuevo técnico" que abre `CreateTechnicianModal`.

---

## Vistas Técnico

### TechnicianDashboard
- Banner de bienvenida personalizado.
- 4 tarjetas de métricas propias.
- Resumen visual de todos sus despliegues con badges de estado.
- Tabla de los 3 despliegues más recientes.

### MyDeployments
- Tabla filtrable (búsqueda + estado) de sus propios despliegues.

### MyProfile
- Formulario editable de nombre, email, departamento y contraseña.
- Modo demo: muestra feedback visual sin persistencia real.

---

## Pending (Práctica 2)

- Reemplazar `mockUsers.js` por llamadas a Spring Security + JWT.
- Endpoint POST `/api/users` para crear técnicos con contraseña hasheada (BCrypt).
- Interceptor Axios para adjuntar Bearer token en cada petición.
- Página de gestión de despliegues conectada al endpoint real `/api/deployments`.
- Logout que invalida el token en el backend (blacklist o refresh token).
