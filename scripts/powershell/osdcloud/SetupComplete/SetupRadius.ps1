# ============================================================
#  SetupRadius.ps1 - BlueDeploy Deployment
#  Activa 802.1X / RADIUS en la interfaz cableada.
#  Debe ejecutarse ANTES de JoinDomain.ps1
# ============================================================

Write-Host ""
Write-Host "================================================="
Write-Host " CONFIGURACION RADIUS / 802.1X - BLUEDEPLOY"
Write-Host "================================================="
Write-Host ""

# -----------------------------------------------------------------------
# 0. CONFIGURACIÓN DE ARQUITECTURA DE EVIDENCIAS (BLUE DEPLOY)
# -----------------------------------------------------------------------

$NextScript    = "C:\OSDCloud\Scripts\SetupComplete\JoinDomain.ps1"
$NextTask      = "JoinDomain"
$LogFile       = "C:\OSDCloud\Logs\SetupRadius.log"

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
        "sourceScript" = "SetupRadius.ps1"
        "payload"      = @{
            "stepName"   = $StepName
            "stepStatus" = $StepStatus
            "message"    = $Message
            "errorCode"  = $FinalErrorCode
            "rawOutput"  = $RawOutput
        }
    }

    $EventData | ConvertTo-Json -Depth 5 | Out-File -FilePath $JsonPath -Encoding utf8 -Force
    Write-Host "[EVIDENCIA] Guardado paso local en: $JsonFileName" -ForegroundColor Gray
}

# -----------------------------------------------------------------------
# 1. CONFIGURAR E INICIAR SERVICIOS RADIUS
# -----------------------------------------------------------------------

Write-Host "Configurando servicios 802.1X..."

$services = @(
    @{ Name = "Dot3svc"; Display = "Wired AutoConfig" },
    @{ Name = "EapHost";  Display = "EAP Host"         }
)

$ServiceErrors = @()
$RawServiceOutput = @()

foreach ($svc in $services) {
    try {
        Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Services\$($svc.Name)" `
                         -Name "Start" -Value 2 -Type DWord -Force | Out-Null

        $s = Get-Service -Name $svc.Name -ErrorAction SilentlyContinue
        if ($s.Status -ne "Running") {
            Start-Service -Name $svc.Name -ErrorAction SilentlyContinue
        }

        $status = (Get-Service -Name $svc.Name).Status
        $RawServiceOutput += "$($svc.Name)=$status"
        Write-Host "  [$status] $($svc.Display)" -ForegroundColor $(if ($status -eq "Running") {"Green"} else {"Red"})

        if ($status -ne "Running") {
            $ServiceErrors += $svc.Name
        }
    } catch {
        $ServiceErrors += $svc.Name
        $RawServiceOutput += "$($svc.Name)=Error($_)"
        Write-Host "  [ERROR] No se pudo iniciar $($svc.Display)" -ForegroundColor Red
    }
}

# [EVIDENCIA 080] Registro del estado de los servicios Wired/EAP
if ($ServiceErrors.Count -eq 0) {
    Write-BlueDeployStep -StepNumber "080" `
                         -StepName "RADIUS_SERVICES_CONFIG" `
                         -StepStatus "SUCCESS" `
                         -Message "802.1X and EAP Host services started successfully" `
                         -RawOutput ($RawServiceOutput -join "; ")
} else {
    Write-BlueDeployStep -StepNumber "080" `
                         -StepName "RADIUS_SERVICES_CONFIG" `
                         -StepStatus "FAILED" `
                         -Message "No se pudieron iniciar los servicios dinámicos de autenticación 802.1X" `
                         -ErrorCode "SERVICES_START_FAILURE" `
                         -RawOutput ($RawServiceOutput -join "; ")
}

# -----------------------------------------------------------------------
# 2. GENERAR Y APLICAR PERFIL XML LAN
# -----------------------------------------------------------------------

Write-Host ""
$xmlPath = "C:\OSDCloud\PerfilLAN8021X.xml"

$xmlContent = @'
<?xml version="1.0"?>
<LANProfile xmlns="http://www.microsoft.com/networking/LAN/profile/v1">
    <MSM>
        <security>
            <OneXEnforced>false</OneXEnforced>
            <OneXEnabled>true</OneXEnabled>
            <OneX xmlns="http://www.microsoft.com/networking/OneX/v1">
                <cacheUserData>false</cacheUserData>
                <authMode>machineOrUser</authMode>
                <EAPConfig>
                    <EapHostConfig xmlns="http://www.microsoft.com/provisioning/EapHostConfig">
                        <EapMethod>
                            <Type xmlns="http://www.microsoft.com/provisioning/EapCommon">25</Type>
                            <VendorId xmlns="http://www.microsoft.com/provisioning/EapCommon">0</VendorId>
                            <VendorType xmlns="http://www.microsoft.com/provisioning/EapCommon">0</VendorType>
                            <AuthorId xmlns="http://www.microsoft.com/provisioning/EapCommon">0</AuthorId>
                        </EapMethod>
                        <ConfigBlob>0100000037000000010000000100000001000000150000001700000000000000000001000000100000001A000000000000000000000000</ConfigBlob>
                    </EapHostConfig>
                </EAPConfig>
            </OneX>
        </security>
    </MSM>
</LANProfile>
'@

Write-Host "Aplicando perfil LAN 802.1X..."
try {
    [xml]$ParsedProfile = $xmlContent
    $xmlContent | Out-File -FilePath $xmlPath -Encoding UTF8 -Force
    $result = netsh lan add profile filename="$xmlPath" 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Perfil LAN aplicado correctamente." -ForegroundColor Green

        # [EVIDENCIA 090 - ÉXITO]
        Write-BlueDeployStep -StepNumber "090" `
                             -StepName "RADIUS_PROFILE_APPLICATION" `
                             -StepStatus "SUCCESS" `
                             -Message "802.1X enterprise LAN network profile applied successfully via netsh" `
                             -RawOutput "ExitCode=0; Output=$result"
    } else {
        Write-Host "[WARN] netsh devolvio: $result" -ForegroundColor Yellow

        # [EVIDENCIA 090 - FALLO DE NETSH]
        Write-BlueDeployStep -StepNumber "090" `
                             -StepName "RADIUS_PROFILE_APPLICATION" `
                             -StepStatus "FAILED" `
                             -Message "Netsh rechazó añadir el perfil de red LAN cableada" `
                             -ErrorCode "NETSH_PROFILE_REJECTED" `
                             -RawOutput "ExitCode=$LASTEXITCODE; Output=$result"
    }
} catch {
    Write-Host "[ERROR] No se pudo aplicar el perfil LAN: $_" -ForegroundColor Red

    # [EVIDENCIA 090 - EXCEPCIÓN]
    Write-BlueDeployStep -StepNumber "090" `
                         -StepName "RADIUS_PROFILE_APPLICATION" `
                         -StepStatus "FAILED" `
                         -Message "Se produjo una excepción crítica al escribir o instalar el archivo del perfil" `
                         -ErrorCode "RADIUS_PROFILE_EXCEPTION" `
                         -RawOutput "Error=$_"
} finally {
    Remove-Item $xmlPath -Force -ErrorAction SilentlyContinue
}

# -----------------------------------------------------------------------
# 3. ELIMINAR ESTA TAREA Y REGISTRAR JOINDOMAIN
# -----------------------------------------------------------------------

Write-Host "Limpiando tarea actual SetupRadius..."
schtasks /delete /tn "SetupRadius" /f | Out-Null

Write-Host "Registrando la proxima tarea programada: $NextTask"
schtasks /create /tn $NextTask `
    /tr "powershell.exe -NoProfile -ExecutionPolicy Bypass -NoExit -File $NextScript" `
    /sc ONLOGON /ru "System" | Out-Null

# [EVIDENCIA 095] Registro del agendamiento exitoso de JoinDomain
Write-BlueDeployStep -StepNumber "095" `
                     -StepName "JOIN_DOMAIN_TASK_REGISTERED" `
                     -StepStatus "SUCCESS" `
                     -Message "La tarea de orquestación JoinDomain se programó correctamente" `
                     -RawOutput "TaskName=$NextTask; Path=$NextScript; RunAs=System"

Write-Host ""
Write-Host "[OK] Proceso terminado. Reiniciando equipo..." -ForegroundColor Green
Write-Host ""

Restart-Computer -Force
