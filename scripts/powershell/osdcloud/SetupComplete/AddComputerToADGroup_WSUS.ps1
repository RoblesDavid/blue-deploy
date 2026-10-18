# ============================================================
#  AddComputerToADGroup_WSUS.ps1 - BlueDeploy Deployment
#  Se ejecuta tras JoinDomain y el primer reinicio en dominio.
#  Fuerza gpupdate, luego agrega el equipo al grupo de AD para
#  politicas WSUS. Al finalizar registra la tarea InstallSoftware.ps1
# ============================================================

Write-Host ""
Write-Host "================================================="
Write-Host "     POLITICAS GPUPDATE Y GRUPO AD - BLUEDEPLOY"
Write-Host "================================================="
Write-Host ""

# -----------------------------------------------------------------------
# 0. CONFIGURACIÓN DE ARQUITECTURA DE EVIDENCIAS Y CREDENCIALES
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
$DeploymentId = "DEPLOY-LAB-$Hostname-$CurrentDate"

# Variables de Dominio y Grupo para entorno de laboratorio / QEMU
$Domain     = if ($env:BLUEDEPLOY_DOMAIN) { $env:BLUEDEPLOY_DOMAIN } else { "lab.local" }
$AdminUser  = if ($env:BLUEDEPLOY_DOMAIN_ADMIN) { $env:BLUEDEPLOY_DOMAIN_ADMIN } else { "lab\Administrator" }
$AdminPass  = if ($env:BLUEDEPLOY_DOMAIN_PASS) { $env:BLUEDEPLOY_DOMAIN_PASS } else { [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('dGVzdDEyMw==')) }
$GroupName  = if ($env:BLUEDEPLOY_WSUS_GROUP) { $env:BLUEDEPLOY_WSUS_GROUP } else { "WSUS_COMPUTERS_GROUP" }

$LogFile    = "C:\OSDCloud\Logs\AddComputerToADGroup_WSUS.log"
$NextScript = "C:\OSDCloud\Scripts\SetupComplete\InstallSoftware.ps1"
$NextTask   = "InstallSoftware"

# Crear directorio de logs si no existe
$LogDir = Split-Path $LogFile -Parent
if (-not (Test-Path $LogDir)) { New-Item -ItemType Directory -Path $LogDir -Force | Out-Null }

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
    $FinalErrorCode = if ([string]::IsNullOrEmpty($ErrorCode)) { $null } else { $ErrorCode }

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
            "errorCode"  = $FinalErrorCode  
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
                         -Message "Las directivas de grupo se actualizaron correctamente en el dispositivo" `
                         -RawOutput "ExitCode=0"
} else {
    Write-Log "gpupdate devolvio un codigo de advertencia o error." "WARN"

    # [EVIDENCIA 120 - ADVERTENCIA/FALLO PARCIAL]
    Write-BlueDeployStep -StepNumber "120" `
                         -StepName "GROUP_POLICY_UPDATE" `
                         -StepStatus "WARNING" `
                         -Message "La actualización de directivas de grupo finalizó con advertencias" `
                         -ErrorCode "GPUPDATE_WARNING" `
                         -RawOutput "Output=$gpResult"
}

# -----------------------------------------------------------------------
# 2. AGREGAR EL EQUIPO AL GRUPO DE ACTIVE DIRECTORY
# -----------------------------------------------------------------------

Write-Log "Comprobando e incluyendo el equipo en el grupo de AD: $GroupName" "INFO"

try {
    Add-Type -AssemblyName System.DirectoryServices -ErrorAction Stop
    $ComputerSAM = "$env:COMPUTERNAME$"

    $LdapHost = if ($Domain) { $Domain } else { "localhost" }
    $RootDse = New-Object System.DirectoryServices.DirectoryEntry("LDAP://$LdapHost/RootDSE", $AdminUser, $AdminPass)
    $NamingContext = [string]$RootDse.Properties["defaultNamingContext"].Value
    if ([string]::IsNullOrWhiteSpace($NamingContext)) {
        throw "No se pudo obtener defaultNamingContext del controlador de dominio."
    }

    $SearchRoot = New-Object System.DirectoryServices.DirectoryEntry("LDAP://$LdapHost/$NamingContext", $AdminUser, $AdminPass)
    $Searcher = New-Object System.DirectoryServices.DirectorySearcher($SearchRoot)
    $Searcher.SearchScope = [System.DirectoryServices.SearchScope]::Subtree
    $Searcher.PropertiesToLoad.Add("distinguishedName") | Out-Null
    $Searcher.PropertiesToLoad.Add("member") | Out-Null

    $Searcher.Filter = "(&(objectCategory=group)(sAMAccountName=$GroupName))"
    $GroupResult = $Searcher.FindOne()
    if (-not $GroupResult) {
        throw "No se encontró el grupo '$GroupName' en Active Directory."
    }

    $Searcher.Filter = "(&(objectCategory=computer)(sAMAccountName=$ComputerSAM))"
    $ComputerResult = $Searcher.FindOne()
    if (-not $ComputerResult) {
        throw "No se encontró el objeto de equipo '$ComputerSAM' en Active Directory."
    }

    $ComputerDn = [string]$ComputerResult.Properties["distinguishedname"][0]
    $GroupEntry = $GroupResult.GetDirectoryEntry()
    $Members = $GroupEntry.Properties["member"]
    if ($Members.Contains($ComputerDn)) {
        Write-Log "El equipo '$ComputerSAM' ya pertenece al grupo '$GroupName'." "OK"
    } else {
        Write-Log "Agregando miembro '$ComputerSAM' al grupo '$GroupName'..." "INFO"
        $Members.Add($ComputerDn) | Out-Null
        $GroupEntry.CommitChanges()
        Write-Log "Equipo agregado al grupo de AD con exito." "OK"
    }

    # [EVIDENCIA 130 - ÉXITO]
    Write-BlueDeployStep -StepNumber "130" `
                         -StepName "AD_GROUP_MEMBERSHIP_MANAGEMENT" `
                         -StepStatus "SUCCESS" `
                         -Message "El objeto del equipo se añadió correctamente al grupo de destino WSUS de AD" `
                         -RawOutput "Group=$GroupName; ComputerSAM=$ComputerSAM; ComputerDN=$ComputerDn"
}
catch {
    $ErrorMsg = $_.Exception.Message
    Write-Log "Error gestionando membresia en el grupo AD: $ErrorMsg" "ERROR"

    # [EVIDENCIA 130 - FALLO]
    Write-BlueDeployStep -StepNumber "130" `
                         -StepName "AD_GROUP_MEMBERSHIP_MANAGEMENT" `
                         -StepStatus "FAILED" `
                         -Message "Error durante la operación de incorporación del equipo al grupo de AD" `
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
$LocalAdmin = "$env:COMPUTERNAME\Administrador"
$TaskRegistered = $false

try {
    $TaskAction = New-ScheduledTaskAction `
        -Execute "$env:SystemRoot\System32\WindowsPowerShell\v1.0\powershell.exe" `
        -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$NextScript`""
    $TaskTrigger = New-ScheduledTaskTrigger -AtLogOn -User $LocalAdmin
    $TaskPrincipal = New-ScheduledTaskPrincipal `
        -UserId $LocalAdmin `
        -LogonType Interactive `
        -RunLevel Highest

    Register-ScheduledTask `
        -TaskName $NextTask `
        -Action $TaskAction `
        -Trigger $TaskTrigger `
        -Principal $TaskPrincipal `
        -Force `
        -ErrorAction Stop | Out-Null

    $TaskRegistered = $true
} catch {
    $taskResult = $_.Exception.Message
}

if ($TaskRegistered) {
    Write-Log "Tarea '$NextTask' registrada correctamente." "OK"

    # [EVIDENCIA 135] Registro del agendamiento de la fase final de software
    Write-BlueDeployStep -StepNumber "135" `
                         -StepName "INSTALL_SOFTWARE_TASK_REGISTERED" `
                         -StepStatus "SUCCESS" `
                         -Message "La tarea del siguiente paso de orquestación para la implementación de software se registró correctamente" `
                         -RawOutput "TaskName=$NextTask; RunAs=$LocalAdmin; LogonType=Interactive; RunLevel=Highest"
} else {
    Write-Log "Fallo al registrar la tarea '$NextTask'. Salida: $taskResult" "ERROR"

    # [EVIDENCIA 135 - FALLO INTERNO]
    Write-BlueDeployStep -StepNumber "135" `
                         -StepName "INSTALL_SOFTWARE_TASK_REGISTERED" `
                         -StepStatus "FAILED" `
                         -Message "El sistema no pudo registrar el desencadenador de la siguiente tarea programada" `
                         -ErrorCode "SCHTASKS_REGISTRATION_FAILURE" `
                         -RawOutput "TaskName=$NextTask; Output=$taskResult"
}

Write-Host ""
Write-Log "Script de politicas y membresia de grupo completado." "OK"
Write-Host ""

Write-Host "Reiniciando en 5 segundos... (Ctrl+C para cancelar)"
Start-Sleep 5
Restart-Computer -Force
