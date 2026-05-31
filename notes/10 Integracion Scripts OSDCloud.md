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

## Revisión de seguridad de scripts

Estado: COMPLETADO

### Objetivo

Revisar la carpeta `scripts/` antes de cerrar el Sprint 5 para evitar incluir credenciales reales o usuarios no permitidos en el repositorio.

### Comprobaciones realizadas

- Búsqueda de usuario literal `Administrador`.
- Búsqueda de contraseñas o secretos en texto plano.
- Búsqueda de cadenas Base64 no autorizadas.
- Revisión de scripts PowerShell y CMD dentro de `scripts/`.

### Resultado

No se encontró ningún usuario literal llamado `Administrador`.

No Se detectaron cadenas Base64 distintas a la autorizada en scripts OSDCloud. 

### Estado final esperado

Los scripts incluidos en el proyecto no contienen credenciales reales ni referencias al usuario de test `OSDAdmin`.

