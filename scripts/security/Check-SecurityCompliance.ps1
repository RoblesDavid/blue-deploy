# ============================================================
#  Check-SecurityCompliance.ps1 - AGSNEG Security (Sprint 5)
#  Objetivo: Verificar Microclaudia, CrowdStrike y la remoción de Autologon.
# ============================================================

# --- Configuración de Entorno e Identidad ---
$TargetDrive  = "C:"
$OsdCloudPath = "$TargetDrive\OSDCloud"
$QueueFolder  = "$TargetDrive\tmp\BlueDeploy\Queue"

# Recuperar el DeploymentId unificado generado en la Fase 1
if (Test-Path "$OsdCloudPath\DeploymentId.txt") {
    $DeploymentId = (Get-Content "$OsdCloudPath\DeploymentId.txt").Trim()
} else {
    $CurrentDate  = Get-Date -Format "yyyyMMdd"
    $DeploymentId = "DEPLOY-AGSNEG-UNKNOWN-PC-$CurrentDate"
}

$Timestamp = (Get-Date -Format "yyyy-MM-ddTHH:mm:ss")
$LogFile   = "$OsdCloudPath\Logs\SecurityCompliance.log"

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $Line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [$Level] $Message"
    $Line | Out-File -FilePath $LogFile -Append
    switch ($Level) {
        "OK"    { Write-Host $Line -ForegroundColor Green }
        "ERROR" { Write-Host $Line -ForegroundColor Red }
        default { Write-Host $Line }
    }
}

Write-Log "Iniciando auditoría de seguridad post-despliegue para: $DeploymentId" "INFO"

# Variables de Control de Estado Global
$GlobalStatus = "PASSED"
$FailureDetails = @()

# -----------------------------------------------------------------------
# COMPROBACIÓN 1: Verificación de Microclaudia
# -----------------------------------------------------------------------
Write-Log "Verificando instalación de Microclaudia..." "INFO"
# Microclaudia opera habitualmente mediante su servicio o su ejecutable en Program Files
$MicroclaudiaService = Get-Service -Name "MicroclaudiaService" -ErrorAction SilentlyContinue
$MicroclaudiaPath    = Test-Path "C:\Program Files\Microclaudia" # Ajustar a tu ruta real si difiere

if ($MicroclaudiaService -or $MicroclaudiaPath) {
    Write-Log "Microclaudia detectado correctamente." "OK"
} else {
    $GlobalStatus = "WARNING"
    $FailureDetails += "Microclaudia_Missing"
    Write-Log "Microclaudia NO se encuentra instalado en el sistema." "ERROR"
}

# -----------------------------------------------------------------------
# COMPROBACIÓN 2: Verificación de CrowdStrike Falcon Sensor
# -----------------------------------------------------------------------
Write-Log "Verificando instalación de CrowdStrike Falcon..." "INFO"
$CrowdStrikeService = Get-Service -Name "CSFalconService" -ErrorAction SilentlyContinue

if ($CrowdStrikeService -and $CrowdStrikeService.Status -eq "Running") {
    Write-Log "CrowdStrike Falcon Sensor operativo y ejecutándose." "OK"
} else {
    $GlobalStatus = "FAILED"
    $FailureDetails += "CrowdStrike_Missing_Or_Stopped"
    Write-Log "CrowdStrike Falcon Sensor NO está instalado o el servicio está detenido." "ERROR"
}

# -----------------------------------------------------------------------
# COMPROBACIÓN 3: Auditoría de Remanentes de AutoAdminLogon (Registro)
# --------------------------------# ---------------------------------------
Write-Log "Auditando vulnerabilidades de AutoLogon en el Registro..." "INFO"
$WinLogonPath = "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon"

$AutoLogonEnabled = Get-ItemPropertyValue -Path $WinLogonPath -Name "AutoAdminLogon" -ErrorAction SilentlyContinue
$DefaultPassword  = Get-ItemPropertyValue -Path $WinLogonPath -Name "DefaultPassword" -ErrorAction SilentlyContinue

# Criterio de Aceptación: AutoAdminLogon debe ser 0 o no existir, y DefaultPassword DEBE haber sido eliminada
if (($AutoLogonEnabled -eq "0" -or $null -eq $AutoLogonEnabled) -and ($null -eq $DefaultPassword)) {
    Write-Log "Registro limpio. No quedan credenciales expuestas de AutoLogon." "OK"
} else {
    $GlobalStatus = "FAILED"
    $FailureDetails += "AutoLogon_Credentials_Exposed"
    Write-Log "CRÍTICO: Se han detectado restos de AutoLogon o contraseñas en texto plano en el registro." "ERROR"
}

# -----------------------------------------------------------------------
# 5. GENERACIÓN DEL JSON DE SEGURIDAD (CONTRATO REST SPRING BOOT)
# -----------------------------------------------------------------------
$JsonFileName = "145_security_compliance_check.json"
$JsonPath     = Join-Path $QueueFolder $JsonFileName

$MessageOutput = if ($GlobalStatus -eq "PASSED") {
    "All mandatory security endpoint agents are installed and AutoLogon registry keys have been securely wiped."
} else {
    "Security compliance gap detected. Failures: $($FailureDetails -join ', ')"
}

# Contrato estricto 'SECURITY_CHECK' para el endpoint de auditorías
$SecurityEvent = [ordered]@{
    "eventType"    = "SECURITY_CHECK"
    "deploymentId" = $DeploymentId
    "apiPath"      = "/api/deployments/$DeploymentId/checks" # Endpoint dedicado a análisis de seguridad
    "createdAt"    = $Timestamp
    "sourceScript" = "Check-SecurityCompliance.ps1"
    "payload"      = @{
        "checkName"     = "FINAL_SECURITY_COMPLIANCE_AUDIT"
        "checkStatus"   = $GlobalStatus   # PASSED, FAILED o WARNING
        "severity"      = if ($GlobalStatus -eq "FAILED") { "CRITICAL" } else { "LOW" }
        "expectedValue" = "Microclaudia=True; CrowdStrike=Running; AutoAdminLogon=Wiped"
        "actualValue"   = "Microclaudia=$($MicroclaudiaPath -or $null -ne $MicroclaudiaService); CrowdStrike=$($null -ne $CrowdStrikeService); Wiped=$(-not $DefaultPassword)"
        "message"       = $MessageOutput
    }
}

$SecurityEvent | ConvertTo-Json -Depth 10 | Out-File -FilePath $JsonPath -Encoding utf8 -Force
Write-Log "Evidencia de seguridad guardada en la cola local: $JsonFileName" "OK"