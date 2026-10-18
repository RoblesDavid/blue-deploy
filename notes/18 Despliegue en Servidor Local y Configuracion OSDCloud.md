# Guía de Despliegue en Servidor Local y Configuración OSDCloud

Esta guía describe paso a paso cómo desplegar la plataforma **BlueDeploy** en un servidor dentro de la red local (LAN) y cómo configurar los scripts de instalación desatendida (**OSDCloud**) en las estaciones de trabajo para que envíen automáticamente toda la telemetría, estados de fase y comprobaciones de seguridad al servidor local.

---

## 1. Arquitectura de Red y Flujo de Comunicación

El flujo de integración entre los clientes OSDCloud y el servidor local BlueDeploy sigue el siguiente esquema:

```text
  ┌──────────────────────────────────────────────────────────────┐
  │                 SERVIDOR LOCAL BLUEDEPLOY                    │
  │                    (IP: 192.168.1.100)                       │
  │                                                              │
  │   ┌─────────────────┐    ┌──────────────────────────────┐    │
  │   │  Frontend Web   │    │     Backend Spring Boot      │    │
  │   │  (React / Vite) │    │       (API REST :8080)       │    │
  │   │  Puerto 80/5173 │    │   - JWT Auth & API Key Auth  │    │
  │   └────────┬────────┘    │   - Auditoría & Reglas RBAC  │    │
  │            │             │   - Diagnóstico IA & PDF Gen │    │
  │            ▼             └──────────────┬───────────────┘    │
  │   ┌─────────────────────────────────────┴───────────────┐    │
  │   │          Base de Datos MariaDB / MySQL              │    │
  │   └─────────────────────────────────────────────────────┘    │
  └──────────────────────────────▲───────────────────────────────┘
                                 │
                   Red Local LAN (HTTP / JSON)
                   Cabecera: X-BlueDeploy-Api-Key
                                 │
  ┌──────────────────────────────┴──────────────────────────────┐
  │               ESTACIÓN DE TRABAJO EN DESPLIEGUE             │
  │                     (Cliente OSDCloud)                      │
  │                                                             │
  │   1. WinPE Boot (01 - Start-OSDCloud.ps1)                   │
  │   2. SetupComplete & Drivers (InstallDrivers.ps1)           │
  │   3. Configuración RADIUS (SetupRadius.ps1)                 │
  │   4. Unión a Dominio AD (JoinDomain.ps1)                    │
  │   5. Software Corporativo (InstallSoftware.ps1)             │
  │   6. Auditoría de Seguridad (Check-SecurityCompliance.ps1)  │
  │                                                             │
  │   Cola local: C:\tmp\BlueDeploy\Queue                       │
  │   Remitente:  Send-BlueDeployQueue.ps1                      │
  └─────────────────────────────────────────────────────────────┘
```

---

## 2. Paso 1: Preparación del Servidor Local

1. **Dirección IP Estática**:
   - Asigna una IP fija al servidor o reserva DHCP en tu router/switch (ejemplo: `192.168.1.100`).
2. **Apertura de Puertos en el Firewall del Servidor**:
   - **Puerto 8080** (API REST Spring Boot) o **Puerto 80** (Nginx / Docker Compose).
   - En Linux (UFW):
     ```bash
     sudo ufw allow 8080/tcp
     sudo ufw allow 80/tcp
     sudo ufw allow 5173/tcp
     ```
   - En Windows Server (PowerShell):
     ```powershell
     New-NetFirewallRule -DisplayName "BlueDeploy Server" -Direction Inbound -LocalPort 8080,80,5173 -Protocol TCP -Action Allow
     ```

---

## 3. Paso 2: Despliegue de la Aplicación en el Servidor

### Opción A: Despliegue con Docker Compose (Recomendado)

1. **Clonar o copiar el repositorio** en el servidor local.
2. **Configurar el archivo `.env`** en la raíz del proyecto:
   ```env
   # Configuración de Base de Datos
   MARIADB_ROOT_PASSWORD=bluedeploy_root_pass
   MARIADB_DATABASE=bluedeploy
   MARIADB_USER=bluedeploy
   MARIADB_PASSWORD=bluedeploy_pass
   DB_HOST=db
   DB_PORT=3306

   # Seguridad y Claves de Autenticación
   JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
   BLUEDEPLOY_API_KEY=bluedeploy-secret-key-change-in-prod
   ```
3. **Iniciar los servicios**:
   ```bash
   docker compose up -d --build
   ```
4. **Verificación de servicios**:
   - Frontend accesible en: `http://192.168.1.100` (o `http://localhost`).
   - Backend API accesible en: `http://192.168.1.100:8080/api/dashboard/summary`.

---

### Opción B: Despliegue en Modo Nativo (Java 21 + Node.js)

1. **Arrancar Base de Datos MariaDB**:
   - Crear base de datos `bluedeploy`.
2. **Iniciar el Backend Spring Boot**:
   ```bash
   export BLUEDEPLOY_API_KEY="bluedeploy-secret-key-change-in-prod"
   export JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
   mvn spring-boot:run
   ```
3. **Iniciar el Frontend React**:
   ```bash
   cd frontend
   npm install
   npm run dev -- --host 0.0.0.0
   ```
   *El parámetro `--host 0.0.0.0` permite el acceso desde cualquier equipo de la red en `http://192.168.1.100:5173`.*

---

## 4. Paso 3: Configuración de los Scripts OSDCloud en el Medio de Instalación (USB/ISO)

Los scripts que se ejecutan en el cliente deben apuntar a la IP del servidor local y utilizar la misma clave de API.

### 4.1. Variables Globales en el Script de Arranque (`01 - Start-OSDCloud.ps1`)

En el script de inicio WinPE, define las variables de entorno de red y seguridad:

```powershell
# Configuración del Servidor Central BlueDeploy
$env:BLUEDEPLOY_API_URL = "http://192.168.1.100:8080"
$env:BLUEDEPLOY_API_KEY = "bluedeploy-secret-key-change-in-prod"
```

### 4.2. Generación de Evidencias y Envío en `InstallSoftware.ps1`

Al finalizar la instalación del software corporativo, el script realiza la auditoría de seguridad y vacía la cola local:

```powershell
# Invocación de la auditoría de cumplimiento
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\Scripts\Security\Check-SecurityCompliance.ps1"

# Envío autenticado de la cola de eventos a la API central
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\tmp\BlueDeploy\Send-BlueDeployQueue.ps1" `
    -QueuePath "C:\tmp\BlueDeploy\Queue" `
    -ApiUrl "http://192.168.1.100:8080" `
    -ApiKey "bluedeploy-secret-key-change-in-prod" `
    -ContinueOnError
```

---

## 5. Paso 4: Comprobación de Alineación de Scripts de Fase 2

Los scripts han sido revisados y ajustados para cumplir con todas las especificaciones de la Fase 2:

| Script | Rol en Fase 2 | Estado y Ajustes Realizados |
| :--- | :--- | :--- |
| **`Send-BlueDeployQueue.ps1`** | Transmisor HTTP de telemetría | ✅ **Alineado**: Envía cabecera `X-BlueDeploy-Api-Key`, soporta `-ApiUrl` y `-ApiKey` por parámetro/entorno, procesa cuerpos `payload` estrictos y purga archivos tras código HTTP 2xx. |
| **`Check-SecurityCompliance.ps1`** | Auditoría de agentes y registro | ✅ **Alineado**: Genera comprobaciones granulares (`MICROCLAUDIA_VALIDATED`, `CROWDSTRIKE_VALIDATED`, `AUTOLOGON_REMOVED`) y la auditoría final `FINAL_SECURITY_COMPLIANCE_AUDIT` en `/api/deployments/{id}/checks`. |
| **`InstallSoftware.ps1`** | Cierre de instalación y limpieza | ✅ **Alineado**: Incluye `apiPath` estricto en la generación de pasos y pasa parámetros seguros a `Send-BlueDeployQueue.ps1`. |
| **`JoinDomain.ps1`** | Integración en Active Directory | ✅ **Alineado**: Genera pasos 100, 110 y 115 con sanitización de valores y captura de errores sin truncado. |
| **`InstallDrivers.ps1`** | Drivers y limpieza bloatware | ✅ **Alineado**: Captura modelo de placa y registra fases 040 a 070. |

---

## 6. Paso 5: Ejecución y Validación de un Despliegue Real

### Prueba de Conectividad previa desde el Cliente (PowerShell):
Antes de iniciar un despliegue completo, puedes probar la conectividad contra el servidor:

```powershell
# 1. Probar conectividad TCP al puerto de la API
Test-NetConnection -ComputerName 192.168.1.100 -Port 8080

# 2. Enviar evento de prueba manual
$Headers = @{
    "Content-Type" = "application/json"
    "X-BlueDeploy-Api-Key" = "bluedeploy-secret-key-change-in-prod"
}

$Body = @{
    deploymentId = "DEPLOY-TEST-MANUAL-001"
    hostname = "PC-LAB-01"
    deviceModel = "Dell OptiPlex 7090"
    domainName = "dominio.local"
} | ConvertTo-Json

Invoke-RestMethod -Method POST -Uri "http://192.168.1.100:8080/api/deployments" -Headers $Headers -Body $Body
```

### Verificación en la Plataforma Web:
1. Accede desde cualquier navegador a `http://192.168.1.100` o `http://192.168.1.100:5173`.
2. Inicia sesión como administrador (`admin` / `admin123`) o como técnico (`carlos` / `carlos123`).
3. Comprueba las siguientes funcionalidades:
   - **Recepción en Vivo**: El equipo aparece en el panel con su modelo, hostname y fase actual.
   - **Comprobaciones de Seguridad**: Las tarjetas de CrowdStrike, Microclaudia y AutoLogon reflejan el resultado de `Check-SecurityCompliance.ps1`.
   - **Autoasignación**: Los técnicos pueden hacer clic en "Asignarme este despliegue" para gestionar el equipo.
   - **Remediación**: En caso de incidencias no conformes, el técnico puede documentar la corrección manual con "Corregir / Remediar".
   - **Diagnóstico IA**: Ante fallos en la instalación, pulsar "Analizar Incidencia con IA" para recibir causa raíz y recomendaciones.
   - **Informe PDF**: Descarga del reporte formal de entrega con "Descargar Informe PDF".
   - **Auditoría**: En `/audit`, comprobar el registro de cada evento recibido desde los scripts y las acciones de los usuarios.

---

## 7. Resolución de Problemas Frecuentes (Troubleshooting)

- **Error `401 Unauthorized` / `API Key no válida`**:
  - Asegúrate de que la cabecera `X-BlueDeploy-Api-Key` enviada por `Send-BlueDeployQueue.ps1` coincide exactamente con el valor `BLUEDEPLOY_API_KEY` definido en el backend.
- **Error `Connection Refused` o Timeout**:
  - Revisa que el firewall del servidor permita el tráfico en el puerto 8080 o 80.
  - Verifica que el backend esté escuchando en `0.0.0.0` y no únicamente en `127.0.0.1`.
- **Los eventos no se borran de `C:\tmp\BlueDeploy\Queue`**:
  - `Send-BlueDeployQueue.ps1` mantiene los archivos en cola si el servidor no responde con un código de éxito (HTTP 200/201), garantizando que no se pierdan telemetrías por caídas temporales de red.
