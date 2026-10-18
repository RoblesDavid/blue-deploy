# ============================================================
#  InstallDrivers.ps1 - BlueDeploy Deployment
#  Se ejecuta en el segundo login via tarea programada.
#  1. Importa drivers desde OSDCloud\Drivers\<Modelo> del USB
#     (omite los que ya estén instalados con igual versión)
#  2. Elimina bloatware de Windows 11
#  3. Elimina impresoras (excepto Microsoft Print to PDF)
#  4. Registra evidencias JSON locales en la cola BlueDeploy
#  5. Registra SetupRadius y reinicia
# ============================================================

Write-Host ""
Write-Host "================================================="
Write-Host " INSTALACION DE DRIVERS Y LIMPIEZA - BLUEDEPLOY"
Write-Host "================================================="
Write-Host ""

$NextScript    = "C:\OSDCloud\Scripts\SetupComplete\SetupRadius.ps1"
$NextTask      = "SetupRadius"
$LogFile       = "C:\OSDCloud\Logs\InstallDrivers.log"

$LogDirectory = Split-Path $LogFile -Parent
if (-not (Test-Path $LogDirectory)) {
    New-Item -ItemType Directory -Path $LogDirectory -Force | Out-Null
}

function Write-Log {
    param(
        [string]$Message,
        [string]$Level = "INFO"
    )

    $Line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [$Level] $Message"
    Add-Content -Path $LogFile -Value $Line -Encoding UTF8
    Write-Host $Line
}

# -----------------------------------------------------------------------
# 0. CONFIGURACIÓN DE ARQUITECTURA DE EVIDENCIAS (BLUE DEPLOY)
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
        "sourceScript" = "InstallDrivers.ps1"
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
# 1. DETECTAR MODELO DEL EQUIPO
# -----------------------------------------------------------------------

Write-Host "--- DETECCION DE MODELO ---"
Write-Host ""

$ComputerModel = (Get-CimInstance -ClassName Win32_ComputerSystem).Model.Trim()
Write-Host "Modelo detectado: $ComputerModel" -ForegroundColor Cyan
Write-Host ""

# [EVIDENCIA 040] Registro del modelo detectado
Write-BlueDeployStep -StepNumber "040" `
                     -StepName "DRIVER_MODEL_DETECTED" `
                     -StepStatus "SUCCESS" `
                     -Message "Se detectó correctamente el modelo del equipo" `
                     -RawOutput "Model=$ComputerModel"

# -----------------------------------------------------------------------
# 2. IMPORTAR DRIVERS
# -----------------------------------------------------------------------

Write-Host "--- DRIVERS ---"
Write-Host ""

$DriverBaseFolder = "OSDCloud\Drivers"
$DriverSource     = $null

$Drives = Get-PSDrive -PSProvider FileSystem | Where-Object { $_.Name -ne 'C' }
foreach ($Drive in $Drives) {
    $Path = Join-Path $Drive.Root "$DriverBaseFolder\$ComputerModel"
    if (Test-Path $Path) {
        $DriverSource = $Path
        break
    }
}

$Total = 0; $Instalados = 0; $Omitidos = 0; $SinCambios = 0; $Errores = 0; $ReinicioRequerido = 0

if (-not $DriverSource) {
    Write-Host "[AVISO] No se encontro carpeta de drivers para el modelo: $ComputerModel" -ForegroundColor Yellow
    Write-Host "        Ruta buscada: <UNIDAD>:\$DriverBaseFolder\$ComputerModel" -ForegroundColor Yellow
    Write-Host "        Continuando sin instalar drivers." -ForegroundColor Yellow

    # [EVIDENCIA 050 - CASO SIN CARPETA]
    Write-BlueDeployStep -StepNumber "050" `
                         -StepName "DRIVER_INSTALLATION" `
                         -StepStatus "WARNING" `
                         -Message "No se encontró la carpeta de controladores para el modelo detectado. La implementación continuó sin instalar controladores." `
                         -ErrorCode "DRIVER_FOLDER_NOT_FOUND" `
                         -RawOutput "Model=$ComputerModel; SearchedPath=<USB>:\$DriverBaseFolder\$ComputerModel"
} else {
    Write-Host "Importando drivers desde: $DriverSource" -ForegroundColor Cyan
    Write-Host ""

    # Inventario de drivers ya instalados (una sola llamada a pnputil)
    $InstalledDrivers = @{}
    $RawOutput        = pnputil /enum-drivers
    $CurrentEntry     = @{}

    foreach ($Line in $RawOutput) {
        if ($Line -match "^Nombre de archivo publicado\s*:\s*(.+)$" -or $Line -match "^Published Name\s*:\s*(.+)$") {
            $CurrentEntry = @{ OemName = $Matches[1].Trim() }
        }
        elseif ($Line -match "^Nombre de archivo original\s*:\s*(.+)$" -or $Line -match "^Original Name\s*:\s*(.+)$") {
            $CurrentEntry['OriginalName'] = $Matches[1].Trim().ToLower()
        }
        elseif ($Line -match "^Versi[oó]n del controlador\s*:\s*(.+)$" -or $Line -match "^Driver Version\s*:\s*(.+)$") {
            if ($Matches[1] -match "(\d+/\d+/\d+)\s+(.+)") {
                $CurrentEntry['DriverDate']    = $Matches[1].Trim()
                $CurrentEntry['DriverVersion'] = $Matches[2].Trim()
            }
        }
        elseif ($Line.Trim() -eq "" -and $CurrentEntry['OriginalName']) {
            $InstalledDrivers[$CurrentEntry['OriginalName']] = $CurrentEntry
            $CurrentEntry = @{}
        }
    }
    if ($CurrentEntry['OriginalName']) { $InstalledDrivers[$CurrentEntry['OriginalName']] = $CurrentEntry }

    Write-Host "Drivers ya instalados en el sistema: $($InstalledDrivers.Count)" -ForegroundColor Gray
    Write-Host ""

    $Drivers = Get-ChildItem -Path $DriverSource -Recurse -Filter "*.inf"
    $Total = $Drivers.Count

    if ($Total -eq 0) {
        Write-Host "[AVISO] La carpeta existe pero no contiene archivos .inf." -ForegroundColor Yellow

        # [EVIDENCIA 050 - CASO SIN INFs]
        Write-BlueDeployStep -StepNumber "050" `
                             -StepName "DRIVER_INSTALLATION" `
                             -StepStatus "WARNING" `
                             -Message "La carpeta de controladores existe, pero no se encontraron archivos .inf." `
                             -ErrorCode "NO_INF_FILES_FOUND" `
                             -RawOutput "Model=$ComputerModel; Total=0"
    } else {
        Write-Host "Procesando $Total archivos .inf encontrados..."

        foreach ($Driver in $Drivers) {
            $CurrentIndex = $Instalados + $Omitidos + $Errores + 1
            $InfName = $Driver.Name.ToLowerInvariant()
            $Counter = "$CurrentIndex/$Total"
            $PercentComplete = [int](($CurrentIndex - 1) * 100 / $Total)
            Write-Progress -Activity "Instalacion de controladores" `
                           -Status "Procesando $Counter : $($Driver.Name)" `
                           -PercentComplete $PercentComplete
            Write-Log "Procesando INF $Counter : $($Driver.FullName)"

            if ($InstalledDrivers.ContainsKey($InfName)) {
                $Installed = $InstalledDrivers[$InfName]
                $InfContent = Get-Content $Driver.FullName -ErrorAction SilentlyContinue
                $DriverVerLine = $InfContent | Where-Object { $_ -match '^\s*DriverVer\s*=' } | Select-Object -First 1

                if ($DriverVerLine -and $DriverVerLine -match '=\s*(.+)') {
                    $UsbVersion = ($Matches[1].Trim() -split ',')[1].Trim()
                    if ($UsbVersion -eq $Installed['DriverVersion']) {
                        Write-Host "[$Counter] OMITIDO (ya instalado v$UsbVersion): $($Driver.Name)" -ForegroundColor Gray
                        Write-Log "Omitido; ya esta instalada la version ${UsbVersion}: $($Driver.Name)" "INFO"
                        $Omitidos++
                        continue
                    }
                    Write-Host "[$Counter] ACTUALIZA (instalado: v$($Installed['DriverVersion']) -> USB: v$UsbVersion): $($Driver.Name)" -ForegroundColor Cyan
                } else {
                    Write-Host "[$Counter] INSTALANDO (version no legible): $($Driver.Name)" -ForegroundColor Yellow
                }
            } else {
                Write-Host "[$Counter] NUEVO: $($Driver.Name)"
            }

            $PnpOutput = & pnputil.exe /add-driver $Driver.FullName /install 2>&1
            $PnpExitCode = $LASTEXITCODE
            $PnpOutputText = $PnpOutput -join "`n"
            $AlreadyCurrent = $PnpOutputText -match '(?i)(actualizado en el dispositivo|est[aá] actualizado en el dispositivo|up to date on the device|is up to date)' -or
                              $PnpOutputText -match '(?i)(paquetes de controladores agregados|driver packages added)\s*:\s*0'

            if ($PnpExitCode -eq 0 -or $PnpExitCode -eq 3010) {
                $Instalados++
                if ($PnpExitCode -eq 3010) {
                    $ReinicioRequerido++
                    Write-Log "Controlador instalado; requiere reinicio: $($Driver.Name)" "WARN"
                } else {
                    Write-Log "Controlador procesado correctamente: $($Driver.Name)"
                }
            } elseif ($PnpExitCode -eq 259 -and $AlreadyCurrent) {
                $SinCambios++
                Write-Log "pnputil no agrego cambios al paquete: $($Driver.Name)"
            } else {
                Write-Host "[ERROR] pnputil fallo con codigo $PnpExitCode para $($Driver.Name): $PnpOutputText" -ForegroundColor Red
                Write-Log "pnputil fallo con codigo $PnpExitCode para $($Driver.Name): $PnpOutputText" "ERROR"
                $Errores++
            }
        }
        Write-Progress -Activity "Instalacion de controladores" -Completed
        Write-Log "Resumen de drivers: Total=$Total; Installed=$Instalados; Skipped=$Omitidos; NoChanges=$SinCambios; RebootRequired=$ReinicioRequerido; Errors=$Errores"

        # Reportar éxito solo cuando pnputil haya instalado o confirmado todos los controladores.
        $Status = "SUCCESS"
        $ErrCode = $null
        if ($Errores -gt 0) {
            $Status = if (($Instalados + $Omitidos + $SinCambios) -eq 0) { "FAILED" } else { "WARNING" }
            $ErrCode = "DRIVER_INSTALL_ERRORS"
        }

        # [EVIDENCIA 050 - CASO EXITOSO O CON ERRORES PARCIALES]
        Write-BlueDeployStep -StepNumber "050" `
                             -StepName "DRIVER_INSTALLATION" `
                             -StepStatus $Status `
                             -Message "El proceso de instalación de controladores finalizó" `
                             -ErrorCode $ErrCode `
                             -RawOutput "Model=$ComputerModel; Total=$Total; Installed=$Instalados; Skipped=$Omitidos; NoChanges=$SinCambios; RebootRequired=$ReinicioRequerido; Errors=$Errores"
    }
}

# -----------------------------------------------------------------------
# 3. ELIMINAR BLOATWARE
# -----------------------------------------------------------------------

Write-Host ""
Write-Host "--- ELIMINACION DE BLOATWARE ---"
Write-Host ""

$BloatwareList = @("Microsoft.BingNews", "Microsoft.SkypeApp", "Microsoft.YourPhone", "Zuffa.UCF")
$RemovedCount  = 0
$NotFoundCount = 0

foreach ($App in $BloatwareList) {
    $Pkg = Get-AppxPackage -Name $App -AllUsers
    if ($Pkg) {
        Write-Host "Eliminando: $App"
        Remove-AppxPackage -Package $Pkg.PackageFullName -AllUsers | Out-Null
        $RemovedCount++
    } else {
        $NotFoundCount++
    }
}

# [EVIDENCIA 060] Registro de Bloatware eliminado
Write-BlueDeployStep -StepNumber "060" `
                     -StepName "BLOATWARE_REMOVAL" `
                     -StepStatus "SUCCESS" `
                     -Message "El proceso de eliminación de bloatware finalizó" `
                     -RawOutput "Removed=$RemovedCount; NotFound=$NotFoundCount"

# -----------------------------------------------------------------------
# 4. ELIMINAR IMPRESORAS DE FABRICA
# -----------------------------------------------------------------------

Write-Host ""
Write-Host "--- LIMPIEZA DE IMPRESORAS ---"
Write-Host ""

$Impresoras      = Get-WmiObject -Class Win32_Printer
$ImpEliminadas   = 0
$ImpConservadas  = 0

$Excluidas = @(
    "Microsoft Print to PDF"
    "Microsoft XPS Document Writer"
    "Fax"
)

foreach ($Impresora in $Impresoras) {
    if ($Excluidas -contains $Impresora.Name) {
        Write-Host "Conservada : $($Impresora.Name)" -ForegroundColor Gray
        $ImpConservadas++
    } else {
        Write-Host "Eliminando : $($Impresora.Name)"
        $Impresora.Delete() | Out-Null
        $ImpEliminadas++
    }
}

# [EVIDENCIA 070] Registro de limpieza de impresoras
Write-BlueDeployStep -StepNumber "070" `
                     -StepName "PRINTER_CLEANUP" `
                     -StepStatus "SUCCESS" `
                     -Message "La limpieza de impresoras predeterminadas finalizó" `
                     -RawOutput "Removed=$ImpEliminadas; Preserved=$ImpConservadas"

# -----------------------------------------------------------------------
# 5. ELIMINAR ESTA TAREA Y REGISTRAR LA SIGUIENTE (SETUPRADIUS)
# -----------------------------------------------------------------------

Write-Log "Limpiando tarea actual InstallDrivers..." "INFO"
schtasks /delete /tn "InstallDrivers" /f | Out-Null

Write-Log "Registrando la proxima tarea programada: $NextTask" "INFO"
schtasks /create /tn $NextTask `
    /tr "powershell.exe -NoProfile -ExecutionPolicy Bypass -NoExit -File $NextScript" `
    /sc ONLOGON /ru "System" | Out-Null

# [EVIDENCIA 075] Registro del agendamiento de la tarea SetupRadius
Write-BlueDeployStep -StepNumber "075" `
                     -StepName "SETUPRADIUS_TASK_REGISTERED" `
                     -StepStatus "SUCCESS" `
                     -Message "La siguiente tarea de orquestación de despliegue se registró correctamente en la programación del sistema" `
                     -RawOutput "TaskName=$NextTask; Path=$NextScript; RunAs=System"

# Finalización del ciclo y reinicio
Write-Host ""
Write-Host "================================================="
Write-Log "Script finalizado correctamente. Reiniciando..." "OK"
Write-Host "================================================="
Write-Host ""

Write-Host "Reiniciando en 5 segundos... (Ctrl+C para cancelar)"
Start-Sleep 5
Restart-Computer -Force
