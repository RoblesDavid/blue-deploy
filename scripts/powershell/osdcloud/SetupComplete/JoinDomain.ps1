# ============================================================
#  JoinDomain.ps1 - BlueDeploy Deployment
#  Une el equipo al dominio de forma automática.
#  Configura autologon para el siguiente reinicio y registra
#  la tarea AddComputerToADGroup_WSUS.
# ============================================================

Write-Host ""
Write-Host "================================================="
Write-Host "         UNION AL DOMINIO - BLUEDEPLOY"
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

# Variables de Dominio para entorno de laboratorio / QEMU
$Domain        = if ($env:BLUEDEPLOY_DOMAIN) { $env:BLUEDEPLOY_DOMAIN } else { "lab.local" }
$OUPath        = if ($env:BLUEDEPLOY_OU) { $env:BLUEDEPLOY_OU } else { "OU=Workstations,DC=lab,DC=local" }
$AdminUser     = if ($env:BLUEDEPLOY_DOMAIN_ADMIN) { $env:BLUEDEPLOY_DOMAIN_ADMIN } else { "lab\Administrator" }
$AdminPass     = if ($env:BLUEDEPLOY_DOMAIN_PASS) { $env:BLUEDEPLOY_DOMAIN_PASS } else { [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('dGVzdDEyMw==')) }

$AutoLogonUser = "Administrador"
$AutoLogonPass = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('dGVzdDEyMw=='))

$NextScript    = "C:\OSDCloud\Scripts\SetupComplete\AddComputerToADGroup_WSUS.ps1"
$NextTask      = "AddComputerToADGroup_WSUS"
$LogFile       = "C:\OSDCloud\Logs\JoinDomain.log"

# Crear directorio de logs si no existe
$LogDir = Split-Path $LogFile -Parent
if (-not (Test-Path $LogDir)) { New-Item -ItemType Directory -Path $LogDir -Force | Out-Null }

function Write-Log {
    param (
        [string]$Message,
        [string]$Status = "INFO"
    )
    $Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $LogMessage = "[$Timestamp] [$Status] $Message"
    $LogMessage | Out-File -FilePath $LogFile -Append -Encoding UTF8

    switch ($Status) {
        "OK"    { Write-Host $LogMessage -ForegroundColor Green }
        "ERROR" { Write-Host $LogMessage -ForegroundColor Red }
        "WARN"  { Write-Host $LogMessage -ForegroundColor Yellow }
        default { Write-Host $LogMessage -ForegroundColor Gray }
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

    $EventData = [ordered]@{
        "eventType"    = "DEPLOYMENT_STEP"
        "deploymentId" = $DeploymentId
        "apiPath"      = "/api/deployments/$DeploymentId/steps"
        "createdAt"    = $Timestamp
        "sourceScript" = "JoinDomain.ps1"
        "payload"      = @{
            "stepName"   = $StepName
            "stepStatus" = $StepStatus
            "message"    = $Message
            "errorCode"  = $ErrorCode
            "rawOutput"  = $RawOutput
        }
    }

    $EventData | ConvertTo-Json -Depth 5 | Out-File -FilePath $JsonPath -Encoding utf8 -Force
    Write-Log "Guardado paso de evidencia local en: $JsonFileName" "OK"
}

# -----------------------------------------------------------------------
# 1. COMPROBAR EL DOMINIO Y ACTIVAR WINDOWS ANTES DE LA UNION
# -----------------------------------------------------------------------

$DomainReady = $false
$SkipJoin = $false
Write-Log "Comprobando la pertenencia actual de $env:COMPUTERNAME al dominio..." "INFO"

try {
    $ComputerSystem = Get-CimInstance -ClassName Win32_ComputerSystem -ErrorAction Stop
    $SkipJoin = $ComputerSystem.PartOfDomain -and ($ComputerSystem.Domain -ieq $Domain)
} catch {
    $DomainCheckError = $_.Exception.Message
    Write-Log "No se pudo comprobar la pertenencia al dominio: $DomainCheckError" "ERROR"
    Write-BlueDeployStep -StepNumber "100" `
                         -StepName "JOIN_DOMAIN_EXECUTION" `
                         -StepStatus "FAILED" `
                         -Message "No se pudo comprobar la pertenencia del equipo al dominio." `
                         -ErrorCode "DOMAIN_MEMBERSHIP_CHECK_FAILURE" `
                         -RawOutput $DomainCheckError
    exit 1
}

if ($SkipJoin) {
    $DomainReady = $true
    Write-Log "El equipo ya pertenece al dominio esperado $Domain; no se repetira la union." "OK"
    Write-BlueDeployStep -StepNumber "098" `
                         -StepName "WINDOWS_ACTIVATION" `
                         -StepStatus "SKIPPED" `
                         -Message "La activación OA3 se omitió porque el equipo ya está unido al dominio esperado." `
                         -RawOutput "AlreadyJoined=True; Domain=$Domain"
    Write-BlueDeployStep -StepNumber "100" `
                         -StepName "JOIN_DOMAIN_EXECUTION" `
                         -StepStatus "SUCCESS" `
                         -Message "El equipo ya estaba unido al dominio esperado; no se intentó una nueva unión." `
                         -RawOutput "Domain=$($ComputerSystem.Domain); AlreadyJoined=True"
} else {
    Write-Log "Fase de activacion OA3 antes de unir al dominio..." "INFO"
    $ActivationStatus = "WARNING"
    $ActivationErrorCode = $null
    $ActivationOutput = ""

    try {
        $ProductKey = (Get-CimInstance -ClassName SoftwareLicensingService -ErrorAction Stop).OA3xOriginalProductKey
        if ([string]::IsNullOrWhiteSpace($ProductKey)) {
            $ActivationOutput = "OA3KeyAvailable=False"
            Write-Log "No se encontro una clave OA3 en el firmware; se continuara con la union." "WARN"
        } else {
            $KeyOutput = & cscript.exe //B "$env:SystemRoot\System32\slmgr.vbs" /ipk $ProductKey 2>&1
            $KeyExitCode = $LASTEXITCODE
            if ($KeyExitCode -ne 0) {
                throw "slmgr /ipk fallo con codigo $KeyExitCode. $($KeyOutput -join ' ')"
            }

            Start-Sleep -Seconds 2
            $ActivationOutput = & cscript.exe //B "$env:SystemRoot\System32\slmgr.vbs" /ato 2>&1
            $ActivationExitCode = $LASTEXITCODE
            if ($ActivationExitCode -eq 0) {
                $ActivationStatus = "SUCCESS"
                Write-Log "Comandos de activacion OA3 enviados correctamente." "OK"
            } else {
                $ActivationErrorCode = "WINDOWS_ACTIVATION_COMMAND_FAILURE"
                Write-Log "El comando de activacion devolvio el codigo $ActivationExitCode." "WARN"
            }
            $ActivationOutput = "OA3KeyInstalled=True; ActivationExitCode=$ActivationExitCode; Output=$($ActivationOutput -join ' ')"
        }
    } catch {
        $ActivationErrorCode = "WINDOWS_ACTIVATION_ERROR"
        $ActivationOutput = $_.Exception.Message
        Write-Log "No se pudo completar la activacion OA3: $ActivationOutput" "WARN"
    }

    Write-BlueDeployStep -StepNumber "098" `
                         -StepName "WINDOWS_ACTIVATION" `
                         -StepStatus $ActivationStatus `
                         -Message "Se intentó la activación OA3 antes de unir el equipo al dominio." `
                         -ErrorCode $ActivationErrorCode `
                         -RawOutput $ActivationOutput

    Write-Log "Iniciando la union al dominio $Domain..." "INFO"
    try {
        $SecurePass = ConvertTo-SecureString $AdminPass -AsPlainText -Force
        $Credential = New-Object System.Management.Automation.PSCredential($AdminUser, $SecurePass)
        Add-Computer -DomainName $Domain -OUPath $OUPath -Credential $Credential -Force -ErrorAction Stop

        $DomainReady = $true
        $SanitizedOU = $OUPath -replace '=', ':' -replace ',', ' | '
        Write-Log "La solicitud de union al dominio se completo correctamente." "OK"
        Write-BlueDeployStep -StepNumber "100" `
                             -StepName "JOIN_DOMAIN_EXECUTION" `
                             -StepStatus "SUCCESS" `
                             -Message "El equipo fue enviado correctamente para unirse al dominio esperado." `
                             -RawOutput "Domain=$Domain; OUPath=$SanitizedOU; AlreadyJoined=False"
    } catch {
        $DomainError = $_.Exception.Message -replace '"', "'" -replace '=', '-'
        Write-Log "No se pudo unir el equipo al dominio. Detalle: $DomainError" "ERROR"
        Write-BlueDeployStep -StepNumber "100" `
                             -StepName "JOIN_DOMAIN_EXECUTION" `
                             -StepStatus "FAILED" `
                             -Message "El equipo no pudo unirse al dominio esperado." `
                             -ErrorCode "DOMAIN_JOIN_FAILURE" `
                             -RawOutput "Domain=$Domain; Error=$DomainError"
    }
}

if (-not $DomainReady) {
    Write-Log "No se programara la fase WSUS porque fallo la union al dominio." "ERROR"
    exit 1
}

# -----------------------------------------------------------------------
# 2. CONFIGURAR AUTOLOGON LOCAL DE TRANSICION
# -----------------------------------------------------------------------

try {
    Write-Log "Configurando AutoLogon local temporal de continuidad..." "INFO"
    $WinLogon = "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon"
    Set-ItemProperty -Path $WinLogon -Name "AutoAdminLogon" -Value "1" -Type String -Force -ErrorAction Stop
    Set-ItemProperty -Path $WinLogon -Name "DefaultUserName" -Value $AutoLogonUser -Type String -Force -ErrorAction Stop
    Set-ItemProperty -Path $WinLogon -Name "DefaultPassword" -Value $AutoLogonPass -Type String -Force -ErrorAction Stop
    Set-ItemProperty -Path $WinLogon -Name "AutoLogonCount" -Value 2 -Type DWord -Force -ErrorAction Stop
    Set-ItemProperty -Path $WinLogon -Name "DefaultDomainName" -Value "." -Type String -Force -ErrorAction Stop

    Write-Log "Autologon configurado para la cuenta local '$AutoLogonUser'." "OK"
    Write-BlueDeployStep -StepNumber "110" `
                         -StepName "JOIN_DOMAIN_AUTOLOGON_CONFIG" `
                         -StepStatus "SUCCESS" `
                         -Message "Las credenciales de AutoLogon de transición se escribieron para la cuenta local de Administrador." `
                         -RawOutput "AutoLogonUser=$AutoLogonUser; Count=2; Domain=."
} catch {
    $AutoLogonError = $_.Exception.Message
    Write-Log "No se pudo configurar AutoLogon: $AutoLogonError" "ERROR"
    Write-BlueDeployStep -StepNumber "110" `
                         -StepName "JOIN_DOMAIN_AUTOLOGON_CONFIG" `
                         -StepStatus "FAILED" `
                         -Message "No se pudo configurar el AutoLogon de transición." `
                         -ErrorCode "AUTOLOGON_CONFIGURATION_FAILURE" `
                         -RawOutput $AutoLogonError
    exit 1
}

# -----------------------------------------------------------------------
# 3. REGISTRAR LA SIGUIENTE TAREA Y REINICIAR
# -----------------------------------------------------------------------

Write-Log "Registrando la proxima tarea programada: $NextTask" "INFO"
$TaskCommand = "powershell.exe -NoProfile -ExecutionPolicy Bypass -File `"$NextScript`""
$TaskOutput = & schtasks.exe /create /tn $NextTask /tr $TaskCommand /sc ONLOGON /ru "SYSTEM" /rl HIGHEST /f 2>&1
$TaskExitCode = $LASTEXITCODE

if ($TaskExitCode -eq 0) {
    Write-Log "Tarea '$NextTask' registrada correctamente bajo SYSTEM." "OK"
    & schtasks.exe /delete /tn "JoinDomain" /f 2>&1 | Out-Null
    Write-BlueDeployStep -StepNumber "115" `
                         -StepName "AD_WSUS_TASK_REGISTERED" `
                         -StepStatus "SUCCESS" `
                         -Message "La siguiente tarea de orquestación de políticas WSUS se registró correctamente." `
                         -RawOutput "TaskName=$NextTask; Path=$NextScript; RunAs=SYSTEM; Trigger=ONLOGON"
} else {
    $TaskError = $TaskOutput -join " "
    Write-Log "Fallo al registrar '$NextTask': $TaskError" "ERROR"
    Write-BlueDeployStep -StepNumber "115" `
                         -StepName "AD_WSUS_TASK_REGISTERED" `
                         -StepStatus "FAILED" `
                         -Message "La siguiente tarea de orquestación de políticas WSUS no pudo registrarse." `
                         -ErrorCode "SCHTASKS_REGISTRATION_FAILURE" `
                         -RawOutput "TaskName=$NextTask; ExitCode=$TaskExitCode; Output=$TaskError"
    exit 1
}

Write-Host ""
Write-Host "================================================="
Write-Log "Fase de dominio completada. Reiniciando..." "OK"
Write-Host "================================================="
Write-Host "Reiniciando en 5 segundos..."
Start-Sleep -Seconds 5
Restart-Computer -Force
