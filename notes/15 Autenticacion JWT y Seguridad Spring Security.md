# Autenticación JWT y Spring Security (Sprint 9 — Práctica 2)

## Propósito

Documenta la implementación de la capa de seguridad y autenticación real mediante **Spring Security 6** y **JWT (JSON Web Tokens)** con Java 21 y Spring Boot 3.2.5. Se sustituye la simulación previa por autenticación sin estado (stateless), cifrado seguro de credenciales con BCrypt, y control de acceso basado en roles (`ADMIN` y `TECHNICIAN`).

---

## Arquitectura de Seguridad

```text
[ Cliente HTTP / Frontend ]
          │
          │ Authorization: Bearer <jwt>
          ▼
┌───────────────────────────────────────────────┐
│           SecurityFilterChain                 │
│  - Deshabilitar CSRF (Stateless REST)        │
│  - SessionCreationPolicy.STATELESS            │
│  - JwtAuthenticationFilter                    │
│    ├─ Valida token y extrae claims/usuario   │
│    └─ Establece SecurityContextHolder        │
│  - DaoAuthenticationProvider                  │
│    ├─ CustomUserDetailsService                │
│    └─ BCryptPasswordEncoder                   │
└───────────────────────────────────────────────┘
          │
          ▼
┌───────────────────────────────────────────────┐
│              Controladores REST               │
│  - /api/auth/login                  → Público │
│  - /api/checks/**                   → Público │
│  - /api/deployments/**              → Público │
│  - /api/dashboard/**                → Público │
│  - /api/users/technicians/**        → ADMIN   │
│  - /api/users/me                    → Auth    │
└───────────────────────────────────────────────┘
```

---

## Componentes Creados y Modificados

1. **`pom.xml`**:
   - `spring-boot-starter-security`
   - `io.jsonwebtoken:jjwt-api:0.12.5`
   - `io.jsonwebtoken:jjwt-impl:0.12.5`
   - `io.jsonwebtoken:jjwt-jackson:0.12.5`
   - `spring-security-test`

2. **DTOs de Autenticación (`com.roblesdavid.blue_deploy.dto`)**:
   - `LoginRequestDTO`: username, password (con validaciones `@NotBlank`).
   - `LoginResponseDTO`: token, tokenType ("Bearer"), expiresIn, id, username, name, email, role, initials, department.

3. **Servicios de Seguridad (`com.roblesdavid.blue_deploy.security`)**:
   - `JwtService`: generación, extracción de claims y validación de tokens JWT mediante firmas HMAC-SHA256 con claves seguras de 256 bits.
   - `CustomUserDetails`: adaptador de la entidad `AppUser` a `UserDetails` de Spring Security, mapeando roles a `ROLE_ADMIN` y `ROLE_TECHNICIAN`.
   - `CustomUserDetailsService`: carga de usuarios desde `AppUserRepository`.
   - `JwtAuthenticationFilter`: filtro `OncePerRequestFilter` que valida la cabecera `Authorization: Bearer <token>` y puebla el `SecurityContextHolder`.

4. **Configuración de Seguridad (`com.roblesdavid.blue_deploy.config.SecurityConfig`)**:
   - Configuración de cadena de filtros HTTP.
   - Configuración de `BCryptPasswordEncoder`.
   - Configuración de `DaoAuthenticationProvider` y `AuthenticationManager`.
   - Configuración de `AuthenticationEntryPoint` (401) y `AccessDeniedHandler` (403) con respuestas JSON limpias.

5. **Controlador y Servicio de Auth (`AuthController` y `AuthService`)**:
   - Endpoint `POST /api/auth/login`.
   - Autenticación con `AuthenticationManager`.
   - Verificación de usuario activo (`active=true`).
   - Retorno de DTO con token JWT firmado y expiración configurada.

6. **Medidas contra Inyecciones SQL**:
   - Uso estricto de Spring Data JPA con `JpaRepository` y PreparedStatement con enlaces de parámetros `:param` automáticos en Hibernate.
   - Sin concatenación dinámica de cadenas en consultas SQL/JPQL.

---

## Matriz de Control de Acceso

| Endpoint | Método | Acceso Requerido | Rol Permitido |
|---|---|---|---|
| `/api/auth/login` | `POST` | Público (`permitAll`) | Todos |
| `/api/checks/**` | `GET`, `POST` | Público (`permitAll` — scripts PowerShell) | Todos |
| `/api/deployments/**` | `GET`, `POST` | Público (`permitAll` — scripts PowerShell) | Todos |
| `/api/dashboard/**` | `GET` | Público / Métricas Dashboard | Todos |
| `/api/users/technicians/**` | `GET`, `POST`, `PUT`, `PATCH` | Autenticado con Rol `ADMIN` | `ADMIN` |
| `/api/users/me` | `GET`, `PUT` | Autenticado | `ADMIN`, `TECHNICIAN` |

---

## Verificación y Tests de Integración

- Suite de autenticación y autorización: `AuthApiIntegrationTest` (9 tests ejecutados y pasando al 100%).
  - Login exitoso con credenciales correctas y emisión de JWT.
  - Login fallido con contraseña errónea (401).
  - Login fallido con usuario inexistente (401).
  - Login fallido con usuario inactivo (401).
  - Validación de campos requeridos vacíos (400).
  - Acceso sin token a endpoints protegidos (401).
  - Acceso de `TECHNICIAN` a endpoints de administración de técnicos (403).
  - Acceso de `ADMIN` a endpoints de administración de técnicos (200 / 201).
  - Acceso y modificación de perfil de `TECHNICIAN` en `/api/users/me` con JWT (200).
