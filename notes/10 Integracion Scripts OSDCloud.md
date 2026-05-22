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
8. Scripts de seguridad post-dominio

## Script actual

### `01 - Start-OSDCloud.ps1`

Estado: EN PROGRESO

JSON esperados:

```json
- `000_deployment.json`
- `010_hostname_captured_step.json`
- `020_windows_installation_step.json`
- `025_unattend_generated_step.json`
- `027_setupcomplete_registered_step.json`
```

Commit posterior:
``` bash
git add notes/10-integracion-scripts-osdcloud.md
git commit -m "docs: add OSDCloud script integration notes"
```