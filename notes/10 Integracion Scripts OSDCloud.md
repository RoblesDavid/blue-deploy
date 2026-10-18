# Integración de scripts OSDCloud con BlueDeploy

## Objetivo

Modificar progresivamente los scripts PowerShell reales de OSDCloud para que generen eventos JSON locales en:

```text
C:\tmp\BlueDeploy\Queue
``` 

Estos JSON serán enviados posteriormente a la API BlueDeploy desde el equipo del operador usando `Send-BlueDeployQueue.ps1`.

## Estrategia

Se modificará y validará un script cada vez.

Orden:

1. `01 - Start-OSDCloud.ps1`
2. `SetupComplete.cmd`
3. `InstallDrivers.ps1`
4. `SetupRadius.ps1`
5. `JoinDomain.ps1`
6. `AddComputerToADGroup_WSUS.ps1`
7. `InstallSoftware.ps1`
8. `Scripts de seguridad post-dominio`

---

## Evolución Práctica 2 — Protección con API Key

Los scripts PowerShell generan eventos JSON locales en `C:\tmp\BlueDeploy\Queue`.
El envío a la API mediante `Send-BlueDeployQueue.ps1` requiere la cabecera HTTP:

```text
X-BlueDeploy-Api-Key: <valor-configurado>
```

### Gestión segura de la API Key:
- La API Key **nunca** debe estar hardcodeada en los scripts versionados de Git.
- El remitente `Send-BlueDeployQueue.ps1` toma automáticamente el valor de `$env:BLUEDEPLOY_API_KEY` o del parámetro seguro `-ApiKey`.
- Si la cabecera falta o es incorrecta, la API responde `HTTP 401 Unauthorized` y genera un registro de auditoría (`INVALID_API_KEY`).

### Endpoints protegidos por API Key / JWT Admin:
- `POST /api/deployments`
- `POST /api/deployments/{deploymentId}/steps`
- `POST /api/deployments/{deploymentId}/checks`

---

## Modelo de Asignación de Despliegues y Técnicos

### ¿Por qué OSDCloud no recoge nombres de técnicos de forma nativa?
1. **Naturaleza Desatendida**: El proceso de despliegue mediante OSDCloud (fases WinPE y `SetupComplete.cmd`) se ejecuta en el hardware de forma desatendida y antes de que exista una sesión de usuario interactiva iniciada por un técnico de soporte. La máquina solo conoce su identidad de hardware (`hostname`, número de serie, modelo, etc.).
2. **Registro Automático**: El script emite el evento inicial creando el despliegue (`technicianId: null`).
3. **Mecanismos de Asignación en BlueDeploy**:
   - **Asignación Centralizada (Administrador)**: Desde el panel de administración (`/admin/deployments`), el responsable de SecOps asigna el equipo al técnico encargado de ese lote o sede (`PUT /api/deployments/{id}/assign-technician/{techId}`).
   - **Autoasignación / Reclamación (Técnico)**: Cuando un técnico toma físicamente o remotamente el equipo para realizar pruebas o resolver incidencias, puede reclamar el despliegue con 1 clic en el panel web ("Asignarme este despliegue" -> `PUT /api/deployments/{id}/claim`).
   - **Parámetro Opcional en Scripts**: En fases manuales post-instalación, `Send-BlueDeployQueue.ps1` puede recibir opcionalmente `-TechnicianUsername` para vincularlo directamente.

---

## Revisión de seguridad y sanitización de scripts

- **Estado**: COMPLETADO
- **Auditoría**:
  - Parámetros de entorno dinámicos (`$env:BLUEDEPLOY_DOMAIN_USER`, `$env:BLUEDEPLOY_DOMAIN_PASS`) con valores mock para pruebas.
  - Ninguna credencial real expuesta en Git.
  - Generación de logs locales y en base de datos protegidos.
