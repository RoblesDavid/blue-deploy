# ============================================================
#  AddComputerToADGroup_WSUS.ps1 - OSD-BlueDeploy Deployment
#  Se ejecuta tras JoinDomain y el primer reinicio en dominio.
#  Fuerza gpupdate, luego agrega el equipo al grupo de AD para
#  politicas WSUS. Al finalizar registra la tarea InstallSoftware.ps1
# ============================================================

Write-Host ""
Write-Host "================================================="
Write-Host "     POLITICAS GPUPDATE Y GRUPO AD - OSD-BlueDeploy"
Write-Host "================================================="
Write-Host ""

# -----------------------------------------------------------------------
# 0. CONFIGURACIÓN DE ARQUITECTURA DE EVIDENCIAS Y CREDENCIALES OFUSCADAS
# -----------------------------------------------------------------------

$QueueFolder = "C:\tmp\BlueDeploy\Queue"
if (-not (Test-Path $QueueFolder)) {
    New-Item -ItemType Directory -Path $QueueFolder -Force | Out-Null
}

# Obtener nombre de equipo guardado en la Fase 1 para armar el DeploymentId consistente
$Hostname = "UNKNOWN-PC"
if (Test-Path "C:\OSDCloud\ComputerName.txt") {
    $Hostname = (Get-Content "C:\OSDCloud\ComputerName.txt").Trim()
}
$CurrentDate  = Get-Date -Format "yyyyMMdd"
$DeploymentId = "DEPLOY-OSD-BlueDeploy-$Hostname-$CurrentDate"

# Credenciales de servicio (Ofuscadas en Base64 para evitar texto plano)
$AdminUser   = "bluedeploy\OSDAdmin_BlueDeploy"
$AdminPass   = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('dGVzdDEyMwo='))
$SecurePass  = ConvertTo-SecureString $AdminPass -AsPlainText -Force
$Credential  = New-Object System.Management.Automation.PSCredential($AdminUser, $SecurePass)

$GroupName   = "GR40_GG_WSUS_W10_PRO"
$LogFile     = "C:\OSDCloud\Logs\AddComputerToADGroup_WSUS.log"
$NextScript  = "C:\OSDCloud\Scripts\SetupComplete\InstallSoftware.ps1"
$NextTask    = "InstallSoftware"

# Crear directorio de logs si no existe
$LogDir = Split-Path $LogFile -Parent
if (-not (Test-Path $LogDir)) { New-Item -ItemType Directory -Path $LogDir -Force | Out-Null }

# Función de log original adaptada para mantener visualización en consola
function Write-Log {
    param(
        [string]$Message,
        [ValidateSet("INFO","OK","WARN","ERROR")]
        [string]$Level = "INFO"
    )
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $line = "[$timestamp][$Level] $Message"
    Add-Content -Path $LogFile -Value $line -Encoding UTF8

    switch ($Level) {
        "OK"    { Write-Host $line -ForegroundColor Green }
        "ERROR" { Write-Host $line -ForegroundColor Red }
        "WARN"  { Write-Host $line -ForegroundColor Yellow }
        default { Write-Host $line -ForegroundColor Gray }
    }
}

# Función interna homologada para asegurar el nombre exacto del archivo JSON (3 dígitos)
function Write-BlueDeployStep {
    param (
        [string]$StepNumber,
        [string]$StepName,
        [string]$StepStatus,
        [string]$Message,
        [string]$ErrorCode = $null,
        [string]$RawOutput = ""
    )

    $Timestamp = (Get-Date -Format "yyyy-MM-ddTHH:mm:ss")

    $CleanStepName = $StepName.ToLower()
    $JsonFileName  = "{0}_{1}_step.json" -f $StepNumber, $CleanStepName
    $JsonPath      = Join-Path $QueueFolder $JsonFileName
    # Si ErrorCode es null o string vacio, forzamos un $null real para evitar el error 400 en la API
    $FinalErrorCode = if ([string]::IsNullOrEmpty($ErrorCode)) { $null } else { $ErrorCode }
    $JsonErrorCode = if ([string]::IsNullOrEmpty($ErrorCode)) { [System.DBNull]::Value } else { $ErrorCode }

    $EventData = [ordered]@{
        "eventType"    = "DEPLOYMENT_STEP"
        "deploymentId" = $DeploymentId
        "apiPath"      = "/api/deployments/$DeploymentId/steps"
        "createdAt"    = $Timestamp
        "sourceScript" = "AddComputerToADGroup_WSUS.ps1"
        "payload"      = @{
            "stepName"   = $StepName
            "stepStatus" = $StepStatus
            "message"    = $Message
            "errorCode"  = $JsonErrorCode  # <--- Asignación con nulidad estricta para el parser de Java
            "rawOutput"  = $RawOutput
        }
    }

    $EventData | ConvertTo-Json -Depth 5 | Out-File -FilePath $JsonPath -Encoding utf8 -Force
    Write-Log "Guardado paso de evidencia local en: $JsonFileName" "OK"
}

# -----------------------------------------------------------------------
# 1. FORZAR ACTUALIZACIÓN DE DIRECTIVAS (GPUPDATE)
# -----------------------------------------------------------------------

Write-Log "Forzando actualizacion de directivas de grupo (gpupdate /force)..." "INFO"
$gpResult = gpupdate /force 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Log "gpupdate ejecutado correctamente." "OK"

    # [EVIDENCIA 120 - ÉXITO]
    Write-BlueDeployStep -StepNumber "120" `
                         -StepName "GROUP_POLICY_UPDATE" `
                         -StepStatus "SUCCESS" `
                         -Message "Group policies updated successfully on the device" `
                         -RawOutput "ExitCode=0"
} else {
    Write-Log "gpupdate devolvio un codigo de advertencia o error." "WARN"

    # [EVIDENCIA 120 - ADVERTENCIA/FALLO PARCIAL]
    Write-BlueDeployStep -StepNumber "120" `
                         -StepName "GROUP_POLICY_UPDATE" `
                         -StepStatus "WARNING" `
                         -Message "Group policies update command completed with warnings" `
                         -ErrorCode "GPUPDATE_WARNING" `
                         -RawOutput "Output=$gpResult"
}

# -----------------------------------------------------------------------
# 2. AGREGAR EL EQUIPO AL GRUPO DE ACTIVE DIRECTORY
# -----------------------------------------------------------------------

Write-Log "Comprobando e incluyendo el equipo en el grupo de AD: $GroupName" "INFO"

try {
    # Importar el módulo de Active Directory de forma explícita por seguridad
    Import-Module ActiveDirectory -ErrorAction Stop

    # Obtener el SamAccountName de la máquina actual
    $ComputerSAM = "$env:COMPUTERNAME$"

    Write-Log "Agregando miembro '$ComputerSAM' al grupo '$GroupName'..." "INFO"
    Add-ADGroupMember -Identity $GroupName -Members $ComputerSAM -Server "bluedeploy.domain.es" -Credential $Credential -ErrorAction Stop
    Write-Log "Equipo agregado al grupo de AD con exito." "OK"

    # [EVIDENCIA 130 - ÉXITO]
    Write-BlueDeployStep -StepNumber "130" `
                         -StepName "AD_GROUP_MEMBERSHIP_MANAGEMENT" `
                         -StepStatus "SUCCESS" `
                         -Message "The computer object was successfully added to the designated WSUS AD Target Group" `
                         -RawOutput "Group=$GroupName; ComputerSAM=$ComputerSAM"
}
catch {
    $ErrorMsg = $_.Exception.Message
    Write-Log "Error gestionando membresia en el grupo AD: $ErrorMsg" "ERROR"
    Write-Log "Tipo de excepcion: $($_.Exception.GetType().Name)" "ERROR"

    # [EVIDENCIA 130 - FALLO]
    Write-BlueDeployStep -StepNumber "130" `
                         -StepName "AD_GROUP_MEMBERSHIP_MANAGEMENT" `
                         -StepStatus "FAILED" `
                         -Message "Critical failure during active directory group attachment operation" `
                         -ErrorCode "AD_GROUP_ADD_FAILURE" `
                         -RawOutput "Group=$GroupName; Error=$ErrorMsg"

    Write-Host "Revisa los privilegios de la cuenta de servicio o la conectividad con el Controlador de Dominio." -ForegroundColor Yellow
}

# --- Pequena pausa para que se sincronice en AD ---
Write-Log "Esperando sincronizacion... (5 segundos)" "INFO"
Start-Sleep -Seconds 5

# -----------------------------------------------------------------------
# 3. ELIMINAR ESTA TAREA Y REGISTRAR LA SIGUIENTE (INSTALLSOFTWARE)
# -----------------------------------------------------------------------

Write-Host ""
Write-Log "Eliminando tarea programada 'AddComputerToADGroup_WSUS'..." "INFO"
try {
    schtasks /delete /tn "AddComputerToADGroup_WSUS" /f 2>&1 | Out-Null
    Write-Log "Tarea 'AddComputerToADGroup_WSUS' eliminada de la lista." "OK"
}
catch {
    Write-Log "Advertencia al eliminar tarea actual: $_" "WARN"
}

Write-Log "Registrando proxima tarea programada: '$NextTask'..." "INFO"
$taskResult = schtasks /create `
    /tn  $NextTask `
    /tr  "powershell.exe -NoProfile -ExecutionPolicy Bypass -NoExit -File `"$NextScript`"" `
    /sc  onlogon `
    /ru  "System" `
    /f   2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Log "Tarea '$NextTask' registrada correctamente." "OK"

    # [EVIDENCIA 135] Registro del agendamiento de la fase final de software
    Write-BlueDeployStep -StepNumber "135" `
                         -StepName "INSTALL_SOFTWARE_TASK_REGISTERED" `
                         -StepStatus "SUCCESS" `
                         -Message "The next orchestration step task for software deployment was registered successfully" `
                         -RawOutput "TaskName=$NextTask; RunAs=System"
} else {
    Write-Log "Fallo al registrar la tarea '$NextTask'. Salida de schtasks: $taskResult" "ERROR"

    # [EVIDENCIA 135 - FALLO INTERNO]
    Write-BlueDeployStep -StepNumber "135" `
                         -StepName "INSTALL_SOFTWARE_TASK_REGISTERED" `
                         -StepStatus "FAILED" `
                         -Message "The OS failed to write the next scheduled task trigger inside the task scheduler structure" `
                         -ErrorCode "SCHTASKS_REGISTRATION_FAILURE" `
                         -RawOutput "TaskName=$NextTask; Output=$taskResult"
}

Write-Host ""
Write-Log "Script de políticas y membresía de grupo completado con éxito." "OK"
Write-Host ""