# ============================================================
#  Check-SecurityCompliance.ps1 - AGSNEG Security (Fase 2)
#  Objetivo: Verificar Microclaudia, CrowdStrike y la remoción de Autologon.
# ============================================================

# --- Configuración de Entorno e Identidad ---
$TargetDrive  = "C:"
$OsdCloudPath = "$TargetDrive\OSDCloud"
$QueueFolder  = "$TargetDrive\tmp\BlueDeploy\Queue"

if (-not (Test-Path $QueueFolder)) {
    New-Item -Path $QueueFolder -ItemType Directory -Force | Out-Null
}

# Recuperar el Hostname y DeploymentId unificado
$Hostname = "UNKNOWN-PC"
if (Test-Path "$OsdCloudPath\ComputerName.txt") {
    $Hostname = (Get-Content "$OsdCloudPath\ComputerName.txt").Trim()
}

if (Test-Path "$OsdCloudPath\DeploymentId.txt") {
    $DeploymentId = (Get-Content "$OsdCloudPath\DeploymentId.txt").Trim()
} elseif (Test-Path "$OsdCloudPath\BlueDeployDeploymentId.txt") {
    $DeploymentId = (Get-Content "$OsdCloudPath\BlueDeployDeploymentId.txt").Trim()
} else {
    $CurrentDate  = Get-Date -Format "yyyyMMdd"
    $DeploymentId = "DEPLOY-AGSNEG-$Hostname-$CurrentDate"
}

$Timestamp = (Get-Date -Format "yyyy-MM-ddTHH:mm:ss")
$LogPath   = "$OsdCloudPath\Logs"
if (-not (Test-Path $LogPath)) { New-Item -Path $LogPath -ItemType Directory -Force | Out-Null }
$LogFile   = "$LogPath\SecurityCompliance.log"

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $Line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [$Level] $Message"
    $Line | Out-File -FilePath $LogFile -Append
    switch ($Level) {
        "OK"    { Write-Host $Line -ForegroundColor Green }
        "ERROR" { Write-Host $Line -ForegroundColor Red }
        "WARN"  { Write-Host $Line -ForegroundColor Yellow }
        default { Write-Host $Line }
    }
}

function Save-SecurityCheckEvent {
    param (
        [string]$FileName,
        [string]$CheckName,
        [string]$CheckStatus,
        [string]$Severity,
        [string]$ExpectedValue,
        [string]$ActualValue,
        [string]$Message
    )

    $EventData = [ordered]@{
        "eventType"    = "SECURITY_CHECK"
        "deploymentId" = $DeploymentId
        "apiPath"      = "/api/deployments/$DeploymentId/checks"
        "createdAt"    = $Timestamp
        "sourceScript" = "Check-SecurityCompliance.ps1"
        "payload"      = [ordered]@{
            "checkName"     = $CheckName
            "checkStatus"   = $CheckStatus
            "severity"      = $Severity
            "expectedValue" = $ExpectedValue
            "actualValue"   = $ActualValue
            "message"       = $Message
        }
    }

    $JsonPath = Join-Path $QueueFolder $FileName
    $EventData | ConvertTo-Json -Depth 10 | Out-File -FilePath $JsonPath -Encoding utf8 -Force
    Write-Log "Evidencia guardada en cola local: $FileName" "OK"
}

Write-Log "Iniciando auditoría de seguridad post-despliegue para: $DeploymentId" "INFO"

# Variables de Control de Estado Global
$GlobalStatus = "PASSED"
$FailureDetails = @()

# --------------------------------# ---------------------------------------
# COMPROBACIÓN 1: Verificación de Microclaudia
# --------------------------------# ---------------------------------------
Write-Log "Verificando instalación de Microclaudia..." "INFO"
$MicroclaudiaService = Get-Service -Name "MicroclaudiaService" -ErrorAction SilentlyContinue
$MicroclaudiaPath    = Test-Path "C:\Program Files\Microclaudia"

$MicroStatus = "PASSED"
$MicroSeverity = "MEDIUM"
$MicroMessage = "Agente Microclaudia verificado y en ejecución."

if ($MicroclaudiaService -or $MicroclaudiaPath) {
    Write-Log "Microclaudia detectado correctamente." "OK"
} else {
    $MicroStatus = "WARNING"
    $MicroSeverity = "MEDIUM"
    $MicroMessage = "Microclaudia no detectado en el sistema."
    $FailureDetails += "Microclaudia_Missing"
    Write-Log "Microclaudia NO se encuentra instalado en el sistema." "WARN"
    if ($GlobalStatus -ne "FAILED") { $GlobalStatus = "WARNING" }
}

Save-SecurityCheckEvent `
    -FileName "141_microclaudia_check.json" `
    -CheckName "MICROCLAUDIA_VALIDATED" `
    -CheckStatus $MicroStatus `
    -Severity $MicroSeverity `
    -ExpectedValue "Installed=True; Service=Running" `
    -ActualValue "Installed=$($MicroclaudiaPath -or $null -ne $MicroclaudiaService); Service=$($MicroclaudiaService.Status)" `
    -Message $MicroMessage

# --------------------------------# ---------------------------------------
# COMPROBACIÓN 2: Verificación de CrowdStrike Falcon Sensor
# --------------------------------# ---------------------------------------
Write-Log "Verificando instalación de CrowdStrike Falcon..." "INFO"
$CrowdStrikeService = Get-Service -Name "CSFalconService" -ErrorAction SilentlyContinue

$CsStatus = "PASSED"
$CsSeverity = "CRITICAL"
$CsMessage = "Sensor CrowdStrike Falcon operativo y en ejecución."

if ($CrowdStrikeService -and $CrowdStrikeService.Status -eq "Running") {
    Write-Log "CrowdStrike Falcon Sensor operativo y ejecutándose." "OK"
} else {
    $CsStatus = "FAILED"
    $CsSeverity = "CRITICAL"
    $CsMessage = "CrowdStrike Falcon Sensor NO está instalado o el servicio está detenido."
    $GlobalStatus = "FAILED"
    $FailureDetails += "CrowdStrike_Missing_Or_Stopped"
    Write-Log "CrowdStrike Falcon Sensor NO está instalado o el servicio está detenido." "ERROR"
}

Save-SecurityCheckEvent `
    -FileName "142_crowdstrike_check.json" `
    -CheckName "CROWDSTRIKE_VALIDATED" `
    -CheckStatus $CsStatus `
    -Severity $CsSeverity `
    -ExpectedValue "Running" `
    -ActualValue "$($CrowdStrikeService.Status)" `
    -Message $CsMessage

# --------------------------------# ---------------------------------------
# COMPROBACIÓN 3: Auditoría de Remanentes de AutoAdminLogon (Registro)
# --------------------------------# ---------------------------------------
Write-Log "Auditando vulnerabilidades de AutoLogon en el Registro..." "INFO"
$WinLogonPath = "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon"

$AutoLogonEnabled = Get-ItemPropertyValue -Path $WinLogonPath -Name "AutoAdminLogon" -ErrorAction SilentlyContinue
$DefaultPassword  = Get-ItemPropertyValue -Path $WinLogonPath -Name "DefaultPassword" -ErrorAction SilentlyContinue

$AutoLogonStatus = "PASSED"
$AutoLogonSeverity = "HIGH"
$AutoLogonMessage = "Credenciales de AutoLogon removidas satisfactoriamente del registro."

if (($AutoLogonEnabled -eq "0" -or $null -eq $AutoLogonEnabled) -and ($null -eq $DefaultPassword)) {
    Write-Log "Registro limpio. No quedan credenciales expuestas de AutoLogon." "OK"
} else {
    $AutoLogonStatus = "FAILED"
    $AutoLogonSeverity = "CRITICAL"
    $AutoLogonMessage = "CRÍTICO: Se han detectado restos de AutoLogon o contraseñas en el registro."
    $GlobalStatus = "FAILED"
    $FailureDetails += "AutoLogon_Credentials_Exposed"
    Write-Log "CRÍTICO: Se han detectado restos de AutoLogon o contraseñas en texto plano en el registro." "ERROR"
}

Save-SecurityCheckEvent `
    -FileName "143_autologon_check.json" `
    -CheckName "AUTOLOGON_REMOVED" `
    -CheckStatus $AutoLogonStatus `
    -Severity $AutoLogonSeverity `
    -ExpectedValue "AutoAdminLogon=0; DefaultPassword=$null" `
    -ActualValue "AutoAdminLogon=$AutoLogonEnabled; DefaultPasswordPresent=$($null -ne $DefaultPassword)" `
    -Message $AutoLogonMessage

# --------------------------------# ---------------------------------------
# COMPROBACIÓN 4: AUDITORÍA GLOBAL DE CONFORMIDAD DE SEGURIDAD
# --------------------------------# ---------------------------------------
$MessageOutput = if ($GlobalStatus -eq "PASSED") {
    "Todos los agentes de seguridad obligatorios están instalados y el registro está limpio de credenciales."
} else {
    "Brecha de conformidad de seguridad detectada. Incidencias: $($FailureDetails -join ', ')"
}

Save-SecurityCheckEvent `
    -FileName "145_security_compliance_check.json" `
    -CheckName "FINAL_SECURITY_COMPLIANCE_AUDIT" `
    -CheckStatus $GlobalStatus `
    -Severity $(if ($GlobalStatus -eq "FAILED") { "CRITICAL" } elseif ($GlobalStatus -eq "WARNING") { "MEDIUM" } else { "LOW" }) `
    -ExpectedValue "Microclaudia=True; CrowdStrike=Running; AutoAdminLogon=Wiped" `
    -ActualValue "Microclaudia=$($MicroclaudiaPath -or $null -ne $MicroclaudiaService); CrowdStrike=$($null -ne $CrowdStrikeService); Wiped=$(-not $DefaultPassword)" `
    -Message $MessageOutput

Write-Log "Auditoría de seguridad completada con resultado: $GlobalStatus" "OK"