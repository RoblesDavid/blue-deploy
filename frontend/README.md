# BlueDeploy — Frontend

Dashboard web del sistema de monitorización de despliegues Windows para Blue Team.

---

## Stack

| Tecnología | Versión | Uso |
|-----------|---------|-----|
| React | 19.2.5 | Framework UI |
| Vite | 8.0.10 | Build tool / Dev server |
| React Router DOM | 7.14.2 | Enrutamiento SPA |
| Tailwind CSS | 4.3.0 | Estilos utilitarios |
| lucide-react | – | Iconografía SVG |
| Chart.js + react-chartjs-2 | 4.5.1 | Gráficos |

---

## Ejecución local

```bash
npm install
npm run dev
# → http://localhost:5173
```

---

## Usuarios de prueba (modo demo — Práctica 1)

| Rol | Usuario | Contraseña |
|-----|---------|------------|
| Administrador | `admin` | `admin` |
| Técnico (Carlos) | `carlos` | `tecnico1` |
| Técnico (Ana) | `ana` | `tecnico2` |

Los datos de despliegues son estáticos y viven en `src/data/mockDeployments.js`.
La autenticación es mock: las credenciales se comparan contra `src/data/mockUsers.js`.

---

## Estructura de directorios

```
src/
├── auth/
│   ├── AuthContext.jsx        # Contexto global de autenticación (mock)
│   └── ProtectedRoute.jsx     # Guard de ruta con comprobación de rol
├── components/
│   ├── layout/
│   │   ├── Sidebar.jsx        # Navegación lateral adaptada al rol
│   │   ├── Header.jsx         # Barra superior con título dinámico
│   │   └── Layout.jsx         # Contenedor Sidebar + Header + <Outlet />
│   ├── common/
│   │   ├── Badge.jsx          # Etiquetas de estado / severidad con colores
│   │   └── StatCard.jsx       # Tarjeta de métrica con icono
│   ├── deployments/
│   │   ├── DeploymentTable.jsx   # Tabla con apertura de panel de detalle
│   │   └── DeploymentDetail.jsx  # Panel lateral deslizante con fases y checks
│   └── modals/
│       └── CreateTechnicianModal.jsx  # Modal de alta de técnico (UI demo)
├── data/
│   ├── mockUsers.js           # 3 usuarios mock (1 admin + 2 técnicos)
│   └── mockDeployments.js     # 7 despliegues mock con fases y checks
├── pages/
│   ├── Login.jsx              # Pantalla de acceso con acceso rápido demo
│   ├── admin/
│   │   ├── AdminDashboard.jsx     # Métricas globales + gráfico por técnico
│   │   ├── AllDeployments.jsx     # Tabla completa con filtros
│   │   └── TechnicianManagement.jsx  # Tarjetas de técnicos + modal de alta
│   └── technician/
│       ├── TechnicianDashboard.jsx  # Dashboard personal
│       ├── MyDeployments.jsx        # Mis despliegues con filtros
│       └── MyProfile.jsx            # Edición de perfil (demo)
├── App.jsx                    # Enrutamiento con roles
└── index.css                  # @import "tailwindcss"
```

---

## Flujo de roles

```
Login
  ├── admin      → /admin      (Dashboard global, todos los despliegues, gestión de técnicos)
  ├── carlos     → /technician (Sus despliegues, su perfil)
  └── ana        → /technician (Sus despliegues, su perfil)
```

El administrador ve los despliegues de todos los técnicos.
El técnico solo ve los suyos.

---

## Pendiente (Práctica 2)

- Reemplazar autenticación mock por JWT + Spring Security.
- Conectar `DeploymentTable` al endpoint real `/api/deployments`.
- Implementar persistencia real en `CreateTechnicianModal`.
- Añadir interceptor Axios con Bearer token.
