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

:: --- Activar cuenta OSDAdmin local y establecer contrasena  ---
echo Activando cuenta OSDAdmin...
net user OSDAdmin /active:yes >nul

:: Establecemos la contraseña decodificándola al vuelo
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$p=[System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('dGVzdDEyMwo=')); $u=[adsi]'WinNT://./OSDAdmin,user'; $u.SetPassword($p); $u.SetInfo()"

:: Desactivar caducidad de contrasena
powershell.exe -NoProfile -Command "Set-LocalUser -Name 'OSDAdmin' -PasswordNeverExpires $true"
echo [OK] Cuenta OSDAdmin activada de forma segura.

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

:: --- Registrar InstallDrivers ---
echo Registrando tarea InstallDrivers...
schtasks /create /tn "InstallDrivers" /tr "powershell.exe -NoProfile -ExecutionPolicy Bypass -NoExit -File C:\OSDCloud\Scripts\SetupComplete\InstallDrivers.ps1" /sc onlogon /ru "SYSTEM" /f >nul
echo [OK] Tarea InstallDrivers registrada bajo la cuenta SYSTEM.

:: --- Configurar AutoLogon seguro para el siguiente reinicio ---
echo Configurando AutoLogon...
reg add "HKLM\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon" /v AutoAdminLogon /t REG_SZ /d "1" /f >nul
reg add "HKLM\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon" /v DefaultUserName /t REG_SZ /d "OSDAdmin" /f >nul
reg add "HKLM\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon" /v AutoLogonCount /t REG_DWORD /d 5 /f >nul

:: Inyectamos la contraseña en el registro decodificándola
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$p=[System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('dGVzdDEyMwo=')); New-ItemProperty -Path 'HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon' -Name 'DefaultPassword' -PropertyType String -Value $p -Force | Out-Null"
echo [OK] AutoLogon configurado.

REM === BlueDeploy JSON Queue - SetupComplete ===
set BLUEDEPLOY_QUEUE=C:\tmp\BlueDeploy\Queue
set BLUEDEPLOY_STATE=C:\OSDCloud
set BLUEDEPLOY_TEMP=C:\tmp\BlueDeploy
set BLUEDEPLOY_EVENT_SCRIPT=C:\tmp\BlueDeploy\Write-SetupCompleteEvent.ps1
set BLUEDEPLOY_DEPLOYMENT_ID_FILE=C:\OSDCloud\BlueDeployDeploymentId.txt

if not exist "%BLUEDEPLOY_QUEUE%" mkdir "%BLUEDEPLOY_QUEUE%"
if not exist "%BLUEDEPLOY_TEMP%" mkdir "%BLUEDEPLOY_TEMP%"

echo Generando JSON de evento BlueDeploy...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$deploymentIdFile = '%BLUEDEPLOY_DEPLOYMENT_ID_FILE%'; $queuePath = '%BLUEDEPLOY_QUEUE%'; $eventPath = Join-Path $queuePath '030_setupcomplete_step.json'; if (Test-Path $deploymentIdFile) { $deploymentId = (Get-Content $deploymentIdFile -Raw).Trim() } else { $deploymentId = 'DEPLOY-' + $env:COMPUTERNAME + '-' + (Get-Date -Format 'yyyyMMdd-HHmmss') }; $event = [ordered]@{ eventType = 'DEPLOYMENT_STEP'; deploymentId = $deploymentId; apiPath = '/api/deployments/' + $deploymentId + '/steps'; createdAt = (Get-Date).ToString('s'); sourceScript = 'SetupComplete.cmd'; payload = [ordered]@{ stepName = 'SETUPCOMPLETE_EXECUTED'; stepStatus = 'SUCCESS'; message = 'SetupComplete executed successfully. Autologon and InstallDrivers task were configured.'; errorCode = $null; rawOutput = 'SetupComplete.cmd executed; InstallDrivers task registered; AutoLogon configured' } }; $event | ConvertTo-Json -Depth 10 | Out-File -FilePath $eventPath -Encoding UTF8 -Force"

echo [OK] Proceso SetupComplete finalizado.

:: --- Reiniciar para continuar la instalacion ---
echo Reiniciando en 5 segundos....
shutdown /r /t 5 /c "Reinicio post-instalacion para inicializar la instalacion de drivers, eliminar bloatware e impresoras por defecto."