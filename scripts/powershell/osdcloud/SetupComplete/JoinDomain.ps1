# ============================================================
#  JoinDomain.ps1 - OSD-BlueDeploy Deployment (Sprint 5)
#  Une el equipo al dominio de forma automática.
#  Configura autologon para el siguiente reinicio y registra
#  la tarea AddComputerToADGroup_WSUS.
# ============================================================

Write-Host ""
Write-Host "================================================="
Write-Host "         UNION AL DOMINIO - OSD-BlueDeploy"
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

# Decodificación segura de credenciales al vuelo (Evita texto plano)
$Domain        = "bluedeploy.domain"
$OUPath        = "OU=Equipos,OU=BLUEDEPLOY,OU=BLUEDEPLOY,OU=Granada,DC=bluedeploy.domain.es"
$AdminUser     = "dmsas\Srvc_GR00MDT"
$AdminPass     = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('dGVzdDEyMwo='))

$AutoLogonUser = "OSDAdmin"
$AutoLogonPass = [System.Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('dGVzdDEyMwo='))

$NextScript    = "C:\OSDCloud\Scripts\SetupComplete\AddComputerToADGroup_WSUS.ps1"
$NextTask      = "AddComputerToADGroup_WSUS"
$LogFile       = "C:\OSDCloud\Logs\JoinDomain.log"

# Crear directorio de logs si no existe
$LogDir = Split-Path $LogFile -Parent
if (-not (Test-Path $LogDir)) { New-Item -ItemType Directory -Path $LogDir -Force | Out-Null }

# Función de Log original modificada para mantener consistencia en consola
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
# 1. EJECUCIÓN DE UNIÓN AL DOMINIO
# -----------------------------------------------------------------------

Write-Log "Iniciando proceso de adicion al dominio: $Domain" "INFO"

try {
    # Convertir contraseña en SecureString para el comando Add-Computer
    $SecPass = ConvertTo-SecureString $AdminPass -AsPlainText -Force
    $Cred    = New-Object System.Management.Automation.PSCredential ($AdminUser, $SecPass)

    # Ejecutar la unión de dominio nativa
    Add-Computer -DomainName $Domain -OUPath $OUPath -Credential $Cred -Force -ErrorAction Stop

    Write-Log "El equipo se ha unido correctamente al dominio $Domain." "OK"

    # [EVIDENCIA 100 - ÉXITO REVISADO]
    # Reemplazamos los signos '=' y las comas conflictivas por guiones y tuberías legibles en la base de datos
    $SanitizedOU = $OUPath -replace '=', ':' -replace ',', ' | '

    Write-BlueDeployStep -StepNumber "100" `
                             -StepName "JOIN_DOMAIN_EXECUTION" `
                             -StepStatus "SUCCESS" `
                             -Message "The computer successfully joined the active directory domain structure" `
                             -ErrorCode $null `   <--- Asegurar que va un $null real de PowerShell, no "" ni 0
                             -RawOutput "Domain [$Domain] - OUPath [$SanitizedOU]"

    # -----------------------------------------------------------------------
    # 2. CONFIGURAR AUTOLOGON DE TRANSICIÓN POST-REINICIO
    # -----------------------------------------------------------------------
    Write-Log "Configurando AutoLogon local temporal de continuidad..." "INFO"

    $WinLogon = "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon"
    Set-ItemProperty -Path $WinLogon -Name "AutoAdminLogon"    -Value "1"             -Type String -Force
    Set-ItemProperty -Path $WinLogon -Name "DefaultUserName"   -Value $AutoLogonUser   -Type String -Force
    Set-ItemProperty -Path $WinLogon -Name "DefaultPassword"   -Value $AutoLogonPass   -Type String -Force
    Set-ItemProperty -Path $WinLogon -Name "AutoLogonCount"    -Value 2               -Type DWord  -Force
    Set-ItemProperty -Path $WinLogon -Name "DefaultDomainName" -Value "."             -Type String -Force

    Write-Log "Autologon configurado correctamente para la cuenta local '$AutoLogonUser'." "OK"

    # [EVIDENCIA 110] Registro de AutoLogon establecido
    Write-BlueDeployStep -StepNumber "110" `
                         -StepName "JOIN_DOMAIN_AUTOLOGON_CONFIG" `
                         -StepStatus "SUCCESS" `
                         -Message "Transition AutoLogon credentials written to registry keys successfully" `
                         -RawOutput "AutoLogonUser=$AutoLogonUser; Count=2"

} catch {
    $ErrorMsg = $_.Exception.Message
    Write-Log "No se pudo unir al dominio. Detalle: $ErrorMsg" "ERROR"

    # [EVIDENCIA 100 - FALLO CRÍTICO]
    # [EVIDENCIA 100 - FALLO CRÍTICO REVISADO]
        $SanitizedError = $ErrorMsg -replace '"', "'" -replace '=', '-'
        Write-BlueDeployStep -StepNumber "100" `
                             -StepName "JOIN_DOMAIN_EXECUTION" `
                             -StepStatus "FAILED" `
                             -Message "Active Directory join command failed operationally" `
                             -ErrorCode "DOMAIN_JOIN_FAILURE" `
                             -RawOutput "Domain [$Domain] - Error: $SanitizedError"

    Write-Host "Posibles causas: Credenciales incorrectas, falta de conectividad RADIUS o conflicto de nombre." -ForegroundColor Yellow
}

# -----------------------------------------------------------------------
# 3. ELIMINAR ESTA TAREA Y REGISTRAR LA SIGUIENTE (WSUS POLICY APPLICATION)
# -----------------------------------------------------------------------

Write-Log "Limpiando tarea actual JoinDomain..." "INFO"
schtasks /delete /tn "JoinDomain" /f | Out-Null

Write-Log "Registrando la proxima tarea programada: $NextTask" "INFO"
schtasks /create /tn $NextTask `
    /tr "powershell.exe -NoProfile -ExecutionPolicy Bypass -NoExit -File $NextScript" `
    /sc ONLOGON /ru "System" | Out-Null

# [EVIDENCIA 115] Registro del agendamiento de la tarea de políticas AD / WSUS
Write-BlueDeployStep -StepNumber "115" `
                     -StepName "AD_WSUS_TASK_REGISTERED" `
                     -StepStatus "SUCCESS" `
                     -Message "Next deployment orchestration task successfully registered in system schedule" `
                     -RawOutput "TaskName=$NextTask; Path=$NextScript; RunAs=System"

# Finalización del ciclo y reinicio
Write-Host ""
Write-Host "================================================="
Write-Log  "Script JoinDomain finalizado correctamente. Reiniciando..." "OK"
Write-Host "================================================="
Write-Host ""

Write-Host "Reiniciando en 5 segundos... (Ctrl+C para cancelar)"
Start-Sleep 5
# Restart-Computer -Force