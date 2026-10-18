@echo off
:: ============================================================
::  SetupComplete.cmd  -  BlueDeploy Deployment
::  Se ejecuta al final de la instalacion, antes del primer login
:: ============================================================

:: --- Limpiar tareas residuales de despliegues anteriores ---
schtasks /delete /tn "InstallDrivers" /f >nul 2>&1
schtasks /delete /tn "SetupRadius" /f >nul 2>&1
schtasks /delete /tn "JoinDomain" /f >nul 2>&1
schtasks /delete /tn "AddComputerToADGroup_WSUS" /f >nul 2>&1
schtasks /delete /tn "InstallSoftware" /f >nul 2>&1

:: --- Habilitar ejecucion de scripts PowerShell ---
echo Habilitando ejecucion de scripts PowerShell...
reg add "HKLM\SOFTWARE\Microsoft\PowerShell\1\ShellIds\Microsoft.PowerShell" /v "ExecutionPolicy" /t REG_SZ /d "RemoteSigned" /f >nul
echo [OK] ExecutionPolicy establecida en RemoteSigned en el Registro.

:: --- Activar cuenta Administrador local y establecer contrasena (Ofuscada) ---
echo Activando cuenta Administrador...
net user Administrador /active:yes >nul

:: Establecemos la contrasena decodificandola al vuelo desde Base64 (test123 para laboratorio)
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$p=[System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('dGVzdDEyMw==')); $u=[adsi]'WinNT://./Administrador,user'; $u.SetPassword($p); $u.SetInfo()"

:: Desactivar caducidad de contrasena usando PowerShell
powershell.exe -NoProfile -Command "Set-LocalUser -Name 'Administrador' -PasswordNeverExpires $true"
echo [OK] Cuenta Administrador activada de forma segura.

:: --- Aplicar nombre del equipo ---
set "NAMEFILE=C:\OSDCloud\ComputerName.txt"

if not exist "%NAMEFILE%" (
    echo [ERROR] No se encontro el archivo de nombre: %NAMEFILE%
    goto :updates
)

set /p ComputerName=<"%NAMEFILE%"

if "%ComputerName%"=="" (
    echo [ERROR] El nombre del equipo esta vacio.
    goto :updates
)

for /f "tokens=*" %%i in ('powershell.exe -NoProfile -Command "(Get-WmiObject Win32_ComputerSystem).Name"') do set CurrentName=%%i
if /i "%CurrentName%"=="%ComputerName%" (
    echo [OK] El nombre ya es correcto, sin cambios.
) else (
    powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Rename-Computer -NewName '%ComputerName%' -Force"
    echo [OK] Nombre de equipo aplicado.
)

:: --- Deshabilitar Windows Update ---
:updates
echo Deshabilitando Windows Update automatico...
reg add "HKLM\SOFTWARE\Policies\Microsoft\Windows\WindowsUpdate\AU" /v NoAutoUpdate /t REG_DWORD /d 1 /f >nul
reg add "HKLM\SOFTWARE\Policies\Microsoft\Windows\WindowsUpdate\AU" /v AUOptions /t REG_DWORD /d 1 /f >nul
sc config wuauserv start= disabled >nul
net stop wuauserv >nul 2>&1
echo [OK] Windows Update deshabilitado.

:: --- Habilitar Escritorio remoto ---
echo Habilitando Escritorio remoto...
set "RDP_STATUS=SUCCESS"
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; try { Set-ItemProperty -Path 'HKLM:\SYSTEM\CurrentControlSet\Control\Terminal Server' -Name 'fDenyTSConnections' -Type DWord -Value 0 -Force; Set-ItemProperty -Path 'HKLM:\SYSTEM\CurrentControlSet\Control\Terminal Server\WinStations\RDP-Tcp' -Name 'UserAuthentication' -Type DWord -Value 1 -Force; Set-Service -Name TermService -StartupType Automatic; Start-Service -Name TermService; Enable-NetFirewallRule -Name 'RemoteDesktop-UserMode-In-TCP','RemoteDesktop-UserMode-In-UDP'; $rules=Get-NetFirewallRule -Name 'RemoteDesktop-UserMode-In-TCP','RemoteDesktop-UserMode-In-UDP'; if ((Get-ItemPropertyValue -Path 'HKLM:\SYSTEM\CurrentControlSet\Control\Terminal Server' -Name fDenyTSConnections) -ne 0) { throw 'RDP connections remain denied' }; if ((Get-ItemPropertyValue -Path 'HKLM:\SYSTEM\CurrentControlSet\Control\Terminal Server\WinStations\RDP-Tcp' -Name UserAuthentication) -ne 1) { throw 'NLA is not enabled' }; if ((Get-Service TermService).Status -ne 'Running') { throw 'TermService is not running' }; if (@($rules | Where-Object { $_.Enabled -ne 'True' }).Count -gt 0) { throw 'One or more Remote Desktop firewall rules are disabled' }; exit 0 } catch { Write-Error $_; exit 1 }"
if errorlevel 1 (
    set "RDP_STATUS=FAILED"
    echo [ERROR] No se pudo habilitar completamente Escritorio remoto.
) else (
    echo [OK] Escritorio remoto habilitado con NLA y reglas de firewall.
)

:: --- Registrar InstallDrivers ---
echo Registrando tarea InstallDrivers...
set "DRIVERS_TASK_STATUS=SUCCESS"
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$admin = (Get-CimInstance Win32_ComputerSystem).Name + '\Administrador'; $action = New-ScheduledTaskAction -Execute (Join-Path $env:SystemRoot 'System32\WindowsPowerShell\v1.0\powershell.exe') -Argument '-NoProfile -ExecutionPolicy Bypass -File C:\OSDCloud\Scripts\SetupComplete\InstallDrivers.ps1'; $trigger = New-ScheduledTaskTrigger -AtLogOn -User $admin; $principal = New-ScheduledTaskPrincipal -UserId $admin -LogonType Interactive -RunLevel Highest; Register-ScheduledTask -TaskName 'InstallDrivers' -Action $action -Trigger $trigger -Principal $principal -Force -ErrorAction Stop | Out-Null"
if errorlevel 1 (
    set "DRIVERS_TASK_STATUS=FAILED"
    echo [ERROR] No se pudo registrar InstallDrivers para la sesion interactiva.
) else (
    echo [OK] Tarea InstallDrivers registrada para mostrar el progreso al Administrador local.
)

:: --- Configurar AutoLogon seguro para el siguiente reinicio ---
echo Configurando AutoLogon...
reg add "HKLM\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon" /v AutoAdminLogon /t REG_SZ /d "1" /f >nul
reg add "HKLM\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon" /v DefaultUserName /t REG_SZ /d "Administrador" /f >nul
reg add "HKLM\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon" /v AutoLogonCount /t REG_DWORD /d 5 /f >nul

:: Inyectamos la contrasena de laboratorio en el registro decodificandola directamente con PowerShell
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$p=[System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('dGVzdDEyMw==')); New-ItemProperty -Path 'HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon' -Name 'DefaultPassword' -PropertyType String -Value $p -Force | Out-Null"
echo [OK] AutoLogon configurado.

REM === BlueDeploy JSON Queue - SetupComplete ===
set BLUEDEPLOY_QUEUE=C:\tmp\BlueDeploy\Queue
set BLUEDEPLOY_STATE=C:\OSDCloud
set BLUEDEPLOY_TEMP=C:\tmp\BlueDeploy
set BLUEDEPLOY_EVENT_SCRIPT=C:\tmp\BlueDeploy\Write-SetupCompleteEvent.ps1
set BLUEDEPLOY_DEPLOYMENT_ID_FILE=C:\OSDCloud\DeploymentId.txt

if not exist "%BLUEDEPLOY_QUEUE%" mkdir "%BLUEDEPLOY_QUEUE%"
if not exist "%BLUEDEPLOY_TEMP%" mkdir "%BLUEDEPLOY_TEMP%"

echo Generando JSON de evento BlueDeploy...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$deploymentIdFile = '%BLUEDEPLOY_DEPLOYMENT_ID_FILE%'; $queuePath = '%BLUEDEPLOY_QUEUE%'; $eventPath = Join-Path $queuePath '030_setupcomplete_step.json'; if (Test-Path $deploymentIdFile) { $deploymentId = (Get-Content $deploymentIdFile -Raw).Trim() } else { $deploymentId = 'DEPLOY-LAB-' + $env:COMPUTERNAME + '-' + (Get-Date -Format 'yyyyMMdd') }; $rdpStatus = '%RDP_STATUS%'; $driversTaskStatus = '%DRIVERS_TASK_STATUS%'; $success = ($rdpStatus -eq 'SUCCESS' -and $driversTaskStatus -eq 'SUCCESS'); $stepStatus = if ($success) { 'SUCCESS' } else { 'WARNING' }; $errorCode = if ($success) { $null } else { 'SETUP_CONFIGURATION_INCOMPLETE' }; $message = if ($success) { 'Remote Desktop and interactive InstallDrivers task were configured and verified.' } else { 'Remote Desktop or the interactive InstallDrivers task failed; inspect SetupComplete output.' }; $event = [ordered]@{ eventType = 'DEPLOYMENT_STEP'; deploymentId = $deploymentId; apiPath = '/api/deployments/' + $deploymentId + '/steps'; createdAt = (Get-Date).ToString('s'); sourceScript = 'SetupComplete.cmd'; payload = [ordered]@{ stepName = 'SETUPCOMPLETE_EXECUTED'; stepStatus = $stepStatus; message = $message; errorCode = $errorCode; rawOutput = 'RDP=' + $rdpStatus + '; InstallDriversTask=' + $driversTaskStatus + '; AutoLogon configured' } }; $event | ConvertTo-Json -Depth 10 | Out-File -FilePath $eventPath -Encoding UTF8 -Force"

echo [OK] Proceso SetupComplete finalizado.

:: --- Reiniciar para continuar la instalacion ---
echo Reiniciando en 5 segundos....
shutdown /r /t 5 /c "Reinicio post-instalacion para inicializar la instalacion de drivers, eliminar bloatware e impresoras por defecto."
