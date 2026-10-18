# 17. Protección de Endpoints de Ingesta con API Key

## Contexto y Motivación
Los scripts de aprovisionamiento en clientes (WinPE, SetupComplete, agentes de auditoría como `Send-BlueDeployQueue.ps1`) envían eventos de telemetría y estado de despliegue a los endpoints de ingesta de la API REST de BlueDeploy.

Para garantizar que únicamente agentes autorizados o administradores del sistema puedan registrar nuevos despliegues, pasos y comprobaciones de seguridad, se ha implementado un mecanismo de autenticación por API Key mediante cabecera HTTP personalizada `X-BlueDeploy-Api-Key`, compatible y coordinado con la seguridad JWT de Spring Security.

---

## Endpoints Protegidos
Los siguientes endpoints de ingesta requieren autenticación mediante API Key válida (`ROLE_API_CLIENT`) o bien un token JWT con rol de Administrador (`ROLE_ADMIN`):

1. **`POST /api/deployments`**: Alta e inicialización de un despliegue.
2. **`POST /api/deployments/{deploymentId}/steps`**: Registro de hitos y pasos de instalación.
3. **`POST /api/deployments/{deploymentId}/checks`**: Registro de auditorías y comprobaciones de seguridad.

Los endpoints de lectura (`GET /api/deployments/**`) continúan siendo públicos o accesibles por usuarios autenticados según la política de acceso.

---

## Arquitectura de Seguridad y Filtros

```
Petición HTTP entrante
        │
        ▼
ApiKeyAuthenticationFilter
  ├── ¿Cabecera X-BlueDeploy-Api-Key presente?
  │     ├── SÍ -> ¿Coincide con BLUEDEPLOY_API_KEY?
  │     │           ├── SÍ: Establece ROLE_API_CLIENT en SecurityContext (aislado por petición)
  │     │           └── NO: Retorna 401 Unauthorized ("API Key no válida")
  │     └── NO: Continúa la cadena de filtros
        │
        ▼
JwtAuthenticationFilter
  ├── ¿Cabecera Authorization: Bearer <token> presente?
  │     ├── SÍ: Valida token y establece ROLE_ADMIN / ROLE_TECHNICIAN
  │     └── NO: Continúa la cadena
        │
        ▼
Spring Security Authorization (SecurityFilterChain)
  ├── POST /api/deployments/** -> hasAnyRole('API_CLIENT', 'ADMIN')
  │     ├── API_CLIENT o ADMIN -> 200 OK
  │     ├── TECHNICIAN sin API Key -> 403 Forbidden
  │     └── Anónimo sin API Key -> 401 Unauthorized
```

---

## Configuración y Variables de Entorno

### Backend (`application.properties` / `application-test.properties`)
```properties
# Configuración de API Key para ingesta de telemetría
bluedeploy.api-key=${BLUEDEPLOY_API_KEY:bluedeploy-secret-key-change-in-prod}
```

### Docker Compose (`docker-compose.yml` y `docker-compose.prod.yml`)
```yaml
environment:
  BLUEDEPLOY_API_KEY: ${BLUEDEPLOY_API_KEY}
```

---

## Actualización en Scripts PowerShell

El script `Send-BlueDeployQueue.ps1` ha sido actualizado para incorporar el parámetro opcional `$ApiKey` con valor por defecto `$env:BLUEDEPLOY_API_KEY`:

```powershell
param(
    [Parameter(Mandatory = $true)]
    [string]$QueuePath,

    [string]$ApiUrl = "http://192.168.18.2:8080",

    [string]$ApiKey = $env:BLUEDEPLOY_API_KEY,

    [switch]$ContinueOnError
)

# Invocación con cabecera X-BlueDeploy-Api-Key
$headers = @{}
if ($ApiKey) {
    $headers["X-BlueDeploy-Api-Key"] = $ApiKey
}

$response = Invoke-RestMethod `
    -Method POST `
    -Uri $uri `
    -ContentType "application/json" `
    -Headers $headers `
    -Body $body
```

---

## Auditoría de Secretos en Scripts
- Se verificó que ningún script almacena claves API en texto plano o valores sensibles codificados que comprometan la seguridad de producción.
- La variable de entorno `$env:BLUEDEPLOY_API_KEY` se alimenta en tiempo de despliegue/ejecución desde el orquestador o variables de entorno seguras.
