# ============================================================
#  InstallSoftware.ps1 - BlueDeploy Deployment
#  Se ejecuta tras las políticas de grupo en el último paso.
#  Instala el software corporativo embebido en el medio/imagen.
#  Genera evidencias finales y gestiona la limpieza de logs.
# ============================================================

Add-Type -AssemblyName PresentationFramework
Add-Type -AssemblyName PresentationCore
Add-Type -AssemblyName WindowsBase

# -----------------------------------------------------------------------
# 0. CONFIGURACIÓN Y EVIDENCIAS
# -----------------------------------------------------------------------

$QueueFolder = "C:\tmp\BlueDeploy\Queue"
if (-not (Test-Path $QueueFolder)) { New-Item -ItemType Directory -Path $QueueFolder -Force | Out-Null }

$Hostname = "UNKNOWN-PC"
if (Test-Path "C:\OSDCloud\ComputerName.txt") { $Hostname = (Get-Content "C:\OSDCloud\ComputerName.txt").Trim() }
$CurrentDate  = Get-Date -Format "yyyyMMdd"
$DeploymentId = "DEPLOY-LAB-$Hostname-$CurrentDate"
$LogPath = "C:\OSDCloud\Logs"
$LogFile = "$LogPath\InstallSoftware.log"

if (-not (Test-Path $LogPath)) { New-Item -ItemType Directory -Path $LogPath -Force | Out-Null }

function Write-Log {
    param([string]$Message, [ValidateSet("INFO","OK","WARN","ERROR","SKIP")]$Level = "INFO")
    $Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $Line = "[$Timestamp] [$Level] $Message"
    Add-Content -Path $LogFile -Value $Line -ErrorAction SilentlyContinue
    
    $Color = "White"
    if ($Level -eq "OK") { $Color = "Green" }
    elseif ($Level -eq "WARN") { $Color = "Yellow" }
    elseif ($Level -eq "ERROR") { $Color = "Red" }
    elseif ($Level -eq "SKIP") { $Color = "Gray" }

    Write-Host $Line -ForegroundColor $Color
}

function Write-BlueDeployStep {
    param ([string]$StepNumber, [string]$StepName, [string]$StepStatus, [string]$Message, [string]$ErrorCode = $null, [string]$RawOutput = "")
    $EventData = [ordered]@{
        "eventType" = "DEPLOYMENT_STEP"
        "deploymentId" = $DeploymentId
        "apiPath" = "/api/deployments/$DeploymentId/steps"
        "createdAt" = (Get-Date -Format "yyyy-MM-ddTHH:mm:ss")
        "sourceScript" = "InstallSoftware.ps1"
        "payload" = @{
            "stepName" = $StepName
            "stepStatus" = $StepStatus
            "message" = $Message
            "errorCode" = $ErrorCode
            "rawOutput" = $RawOutput
        }
    }
    $JsonPath = Join-Path $QueueFolder ("{0}_{1}_step.json" -f $StepNumber, ($StepName.ToLower()))
    $EventData | ConvertTo-Json -Depth 5 | Out-File -FilePath $JsonPath -Encoding utf8 -Force
}

# -----------------------------------------------------------------------
# 1. LOCALIZACIÓN Y LECTURA DE SOFTWARE
# -----------------------------------------------------------------------

$AppsSource = "C:\OSDCloud\Apps"
if (-not (Test-Path $AppsSource)) {
    $Drives = Get-PSDrive -PSProvider FileSystem | Where-Object { $_.Name -ne 'C' }
    foreach ($Drive in $Drives) {
        $Path = Join-Path $Drive.Root "OSDCloud\Apps"
        if (Test-Path $Path) { $AppsSource = $Path; break }
    }
}

$AppList = @()
if (Test-Path $AppsSource) {
    foreach ($Folder in (Get-ChildItem -Path $AppsSource -Directory)) {
        $JsonPath = Join-Path $Folder.FullName "app.json"
        if (Test-Path $JsonPath) {
            try {
                $Json = Get-Content $JsonPath -Raw | ConvertFrom-Json
                $AppList += [PSCustomObject]@{
                    Name = $Json.Name
                    Obligatoria = [bool]($Json.Obligatoria)
                    Directorio = $Folder.FullName
                    Instalador = Join-Path $Folder.FullName $Json.Instalador
                    Argumentos = $Json.Argumentos
                }
            } catch { Write-Log "Error leyendo JSON en $($Folder.Name)" "ERROR" }
        }
    }
}

# -----------------------------------------------------------------------
# 2. INTERFAZ WPF (XAML)
# -----------------------------------------------------------------------

$XamlText = @'
<Window xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
        xmlns:x="http://schemas.microsoft.com/winfx/2006/xaml"
        Title="BlueDeploy - Instalación de Software"
        Width="540"
        Height="600"
        WindowStartupLocation="CenterScreen"
        ResizeMode="NoResize"
        Background="#F4F4F4">

    <Grid Margin="20">
        <Grid.RowDefinitions>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="*"/>
            <RowDefinition Height="Auto"/>
        </Grid.RowDefinitions>

        <StackPanel Grid.Row="0" Margin="0,0,0,14">
            <TextBlock Text="Instalación de Software Corporativo" FontSize="18" FontWeight="Bold"/>
            <TextBlock x:Name="SubTitle" Text="Seleccione las aplicaciones a instalar." Foreground="#555"/>
        </StackPanel>

        <Grid x:Name="PanelSel" Grid.Row="1">
            <Grid.RowDefinitions>
                <RowDefinition Height="*"/>
                <RowDefinition Height="Auto"/>
            </Grid.RowDefinitions>

            <Border Background="White" BorderBrush="#DADADA" BorderThickness="1">
                <ScrollViewer>
                    <StackPanel x:Name="AppPanel" Margin="10"/>
                </ScrollViewer>
            </Border>

            <StackPanel Grid.Row="1" Orientation="Horizontal" Margin="0,10">
                <Button x:Name="BtnAll" Content="Todas" Width="80" Margin="0,0,5,0"/>
                <Button x:Name="BtnNone" Content="Ninguna" Width="80"/>
            </StackPanel>
        </Grid>

        <Border x:Name="PanelProg" Grid.Row="1" Visibility="Collapsed" Background="White" BorderBrush="#DADADA" BorderThickness="1">
            <ScrollViewer x:Name="ScrollP">
                <StackPanel x:Name="ProgPanel" Margin="10"/>
            </ScrollViewer>
        </Border>

        <UniformGrid Grid.Row="2" Columns="2" Margin="0,10,0,0">
            <Button x:Name="BtnSkip" Content="Omitir e Ir a Limpieza" Height="40" Margin="0,0,5,0" Background="#9E9E9E" Foreground="White"/>
            <Button x:Name="BtnInstall" Content="Instalar" Height="40" Background="#1A73E8" Foreground="White" FontWeight="Bold"/>
        </UniformGrid>
    </Grid>
</Window>
'@

$Window = [Windows.Markup.XamlReader]::Parse($XamlText)
$AppPanel = $Window.FindName("AppPanel"); $ProgPanel = $Window.FindName("ProgPanel")
$BtnInstall = $Window.FindName("BtnInstall"); $BtnSkip = $Window.FindName("BtnSkip")
$PanelSel = $Window.FindName("PanelSel"); $PanelProg = $Window.FindName("PanelProg")
$SubTitle = $Window.FindName("SubTitle")

$Checkboxes = @{}
foreach ($App in $AppList) {
    $CB = New-Object System.Windows.Controls.CheckBox
    $CB.Content = $App.Name; $CB.IsChecked = $App.Obligatoria; $CB.Margin = "0,5"
    $AppPanel.Children.Add($CB) | Out-Null
    $Checkboxes[$App.Name] = $CB
}

$Window.FindName("BtnAll").Add_Click({ foreach($C in $Checkboxes.Values){$C.IsChecked=$true} })
$Window.FindName("BtnNone").Add_Click({ foreach($C in $Checkboxes.Values){$C.IsChecked=$false} })

# -----------------------------------------------------------------------
# 3. LÓGICA DE INSTALACIÓN Y EVIDENCIAS
# -----------------------------------------------------------------------

$SoftwareState = [hashtable]::Synchronized(@{
    Started = $false
    SelectedNames = @()
    Results = [System.Collections.ArrayList]::Synchronized((New-Object System.Collections.ArrayList))
})

$BtnSkip.Add_Click({ $Window.Close() })

# Botón Instalar
$BtnInstall.Add_Click({
    if ($BtnInstall.Content -eq "Finalizar") { $Window.Close(); return }

    $SelectedApps = $AppList | Where-Object { $Checkboxes[$_.Name].IsChecked }
    if ($SelectedApps.Count -eq 0) { [System.Windows.MessageBox]::Show("No hay aplicaciones seleccionadas."); return }

    $SoftwareState.Started = $true
    $SoftwareState.SelectedNames = @($SelectedApps | ForEach-Object { $_.Name })

    $PanelSel.Visibility = "Collapsed"; $PanelProg.Visibility = "Visible"
    $BtnInstall.IsEnabled = $false; $BtnSkip.IsEnabled = $false
    $SubTitle.Text = "Instalando aplicaciones..."

    $ProgRows = @{}
    foreach ($App in $SelectedApps) {
        $Lab = New-Object System.Windows.Controls.TextBlock; $Lab.Text = "• $($App.Name): Esperando..."; $ProgPanel.Children.Add($Lab) | Out-Null
        $ProgRows[$App.Name] = $Lab
    }

    $Sync = [hashtable]::Synchronized(@{ Window=$Window; Apps=$SelectedApps; Rows=$ProgRows; Btn=$BtnInstall; Sub=$SubTitle; Log=$LogFile; Results=$SoftwareState.Results })
    $Runspace = [runspacefactory]::CreateRunspace(); $Runspace.ApartmentState = "STA"; $Runspace.Open()
    $Runspace.SessionStateProxy.SetVariable("Sync", $Sync)

    $Script = {
        foreach ($App in $Sync.Apps) {
            $Sync.Window.Dispatcher.Invoke([action]{ $Sync.Rows[$App.Name].Text = "• $($App.Name): Instalando..."; $Sync.Rows[$App.Name].Foreground = "Blue" })

            $ExitCode = -1
            $InstallError = $null
            Add-Content -Path $Sync.Log -Value "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [INFO] Iniciando instalacion: $($App.Name)"
            try {
                $Ext = [System.IO.Path]::GetExtension($App.Instalador).ToLower()
                if ($Ext -eq ".msi") {
                    $Proc = Start-Process "msiexec" -ArgumentList "/i `"$($App.Instalador)`" /qn /norestart $($App.Argumentos)" -Wait -PassThru
                } else {
                    $Proc = Start-Process $App.Instalador -ArgumentList $App.Argumentos -Wait -PassThru -WorkingDirectory $App.Directorio
                }
                $ExitCode = $Proc.ExitCode
            } catch {
                $InstallError = $_.Exception.Message
            }

            $Status = "FAILED"
            $DisplayStatus = "Error"
            $TextColor = "Red"
            if ($ExitCode -eq 0 -or $ExitCode -eq 3010) { 
                $Status = "SUCCESS"
                $DisplayStatus = if ($ExitCode -eq 3010) { "OK (reinicio requerido)" } else { "OK" }
                $TextColor = "Green"
            }

            $Result = [PSCustomObject]@{
                Name = $App.Name
                StepStatus = $Status
                ExitCode = $ExitCode
                Error = $InstallError
            }
            $Sync.Results.Add($Result) | Out-Null
            $LogLevel = if ($Status -eq "SUCCESS") { "OK" } else { "ERROR" }
            Add-Content -Path $Sync.Log -Value "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] [$LogLevel] $($App.Name); ExitCode=$ExitCode; Error=$InstallError"
            
            $Sync.Window.Dispatcher.Invoke([action]{ 
                $Sync.Rows[$App.Name].Text = "• $($App.Name): $DisplayStatus ($ExitCode)"
                $Sync.Rows[$App.Name].Foreground = $TextColor
            })
        }
        $Sync.Window.Dispatcher.Invoke([action]{ 
            $Sync.Btn.Content = "Finalizar"; $Sync.Btn.IsEnabled = $true; $Sync.Sub.Text = "Proceso terminado."
            $Sync.Btn.Background = "SeaGreen"
        })
    }
    $PS = [powershell]::Create().AddScript($Script); $PS.Runspace = $Runspace; $PS.BeginInvoke()
})

$Window.ShowDialog() | Out-Null

$SoftwareResultsByName = @{}
foreach ($Result in $SoftwareState.Results) {
    $SoftwareResultsByName[$Result.Name] = $Result
}

$AppEventNumber = 150
foreach ($App in $AppList) {
    $AppStepStatus = "SKIPPED"
    $AppMessage = "Application was not selected for installation."
    $AppErrorCode = $null
    $AppRawOutput = "Name=$($App.Name); Selected=False"

    if ($SoftwareState.Started -and $SoftwareState.SelectedNames -contains $App.Name) {
        if ($SoftwareResultsByName.ContainsKey($App.Name)) {
            $AppResult = $SoftwareResultsByName[$App.Name]
            $AppStepStatus = $AppResult.StepStatus
            $AppMessage = if ($AppResult.StepStatus -eq "SUCCESS") { "Application installer completed successfully." } else { "Application installer failed." }
            $AppErrorCode = if ($AppResult.StepStatus -eq "FAILED") { "APPLICATION_INSTALL_FAILURE" } else { $null }
            $AppRawOutput = "Name=$($App.Name); Selected=True; ExitCode=$($AppResult.ExitCode); Error=$($AppResult.Error)"
        } else {
            $AppStepStatus = "FAILED"
            $AppMessage = "No completion result was recorded for the selected application."
            $AppErrorCode = "APPLICATION_RESULT_MISSING"
            $AppRawOutput = "Name=$($App.Name); Selected=True; Result=Missing"
        }
    } elseif (-not $SoftwareState.Started) {
        $AppMessage = "Application phase was skipped or closed before installation started."
    }

    $SafeAppName = [regex]::Replace($App.Name.ToUpperInvariant(), '[^A-Z0-9_-]', '_').Trim('_')
    if ([string]::IsNullOrWhiteSpace($SafeAppName)) { $SafeAppName = "APP" }
    $AppStepName = "APPLICATION_INSTALL_$SafeAppName"
    Write-Log "$AppStepName; Status=$AppStepStatus; $AppRawOutput"
    Write-BlueDeployStep -StepNumber ("{0:D3}" -f $AppEventNumber) `
                         -StepName $AppStepName `
                         -StepStatus $AppStepStatus `
                         -Message $AppMessage `
                         -ErrorCode $AppErrorCode `
                         -RawOutput $AppRawOutput
    $AppEventNumber++
}

# -----------------------------------------------------------------------
# 4. APLICAR DESCRIPCIÓN DEL EQUIPO Y DATOS OCS (CORREGIDO)
# -----------------------------------------------------------------------

$EquipoDataPath = "C:\OSDCloud\DatosEquipo.txt"
$EquipoData = @{}
$DataError = $null

try {
    if (-not (Test-Path $EquipoDataPath)) {
        throw "No se encontro el archivo de datos del equipo: $EquipoDataPath"
    }

    foreach ($RawLine in Get-Content -Path $EquipoDataPath -ErrorAction Stop) {
        $Line = $RawLine.Trim().Trim([char]0xFEFF)
        $SeparatorIndex = $Line.IndexOf('=')
        if ($SeparatorIndex -gt 0) {
            $Key = $Line.Substring(0, $SeparatorIndex).Trim().Trim([char]0xFEFF)
            $Value = $Line.Substring($SeparatorIndex + 1).Trim()
            $EquipoData[$Key] = $Value
        }
    }

    $DescripcionEquipo = $EquipoData['Descripcion']
    $CodigoCeges = $EquipoData['CodigoCEGES']
    if ([string]::IsNullOrWhiteSpace($DescripcionEquipo) -or [string]::IsNullOrWhiteSpace($CodigoCeges)) {
        throw "El archivo de datos del equipo no contiene descripcion y codigo CEGES validos."
    }

    # 1. LanmanServer registry (Server comment)
    $LanmanPath = "HKLM:\SYSTEM\CurrentControlSet\Services\LanmanServer\Parameters"
    if (-not (Test-Path $LanmanPath)) { New-Item -Path $LanmanPath -Force -ErrorAction Stop | Out-Null }
    Set-ItemProperty -Path $LanmanPath -Name "srvcomment" -Value $DescripcionEquipo -Type String -Force -ErrorAction Stop | Out-Null

    # 2. Net config server & CIM/WMI OS Description
    try {
        & net.exe config server /srvcomment:"$DescripcionEquipo" | Out-Null
    } catch {
        Write-Log "Aviso al ejecutar net config server: $_" "WARN"
    }
    try {
        Set-CimInstance -Query "Select * from Win32_OperatingSystem" -Property @{ Description = $DescripcionEquipo } -ErrorAction SilentlyContinue | Out-Null
    } catch {}

    # 3. OCS Inventory Agent registry (both 64-bit and WOW6432Node for 32-bit agents)
    $OcsPaths = @("HKLM:\SOFTWARE\OCS", "HKLM:\SOFTWARE\WOW6432Node\OCS")
    foreach ($OcsPath in $OcsPaths) {
        if (-not (Test-Path $OcsPath)) { New-Item -Path $OcsPath -Force -ErrorAction Stop | Out-Null }
        Set-ItemProperty -Path $OcsPath -Name "ubicacion" -Value $DescripcionEquipo -Type String -Force -ErrorAction Stop | Out-Null
        Set-ItemProperty -Path $OcsPath -Name "codigoceges" -Value $CodigoCeges -Type String -Force -ErrorAction Stop | Out-Null
    }

    Write-Log "Descripcion del equipo y valores OCS aplicados correctamente." "OK"
    Write-BlueDeployStep -StepNumber "145" `
                        -StepName "DEVICE_DESCRIPTION_AND_OCS_CONFIG" `
                        -StepStatus "SUCCESS" `
                        -Message "La descripción del equipo y los valores OCS se escribieron en el registro tras la instalación de la aplicación." `
                        -RawOutput "Description=$DescripcionEquipo; CodigoCEGES=$CodigoCeges; Lanman=OK; OCS=OK"
} catch {
    $DataError = $_.Exception.Message
    Write-Log "No se pudieron aplicar la descripcion y los valores OCS: $DataError" "ERROR"
    Write-BlueDeployStep -StepNumber "145" `
                        -StepName "DEVICE_DESCRIPTION_AND_OCS_CONFIG" `
                        -StepStatus "FAILED" `
                        -Message "No se pudieron aplicar la descripción del equipo y los valores OCS." `
                        -ErrorCode "DEVICE_METADATA_REGISTRY_FAILURE" `
                        -RawOutput $DataError
}

# -----------------------------------------------------------------------
# 5. RESUMEN GLOBAL Y FINALIZACIÓN (PASO 999)
# -----------------------------------------------------------------------

$PriorStepEvents = @()
foreach ($EventFile in Get-ChildItem -Path $QueueFolder -Filter "*.json" -File) {
    if ($EventFile.Name -eq "999_deployment_completed_step.json") { continue }

    try {
        $QueuedEvent = Get-Content -Path $EventFile.FullName -Raw | ConvertFrom-Json -ErrorAction Stop
        if ($QueuedEvent.eventType -eq "DEPLOYMENT_STEP" -and $QueuedEvent.payload.stepStatus) {
            $PriorStepEvents += $QueuedEvent
        }
    } catch {
        Write-Log "No se pudo leer evento de BlueDeploy $($EventFile.Name): $_" "WARN"
    }
}

$FailedSteps = @($PriorStepEvents | Where-Object { $_.payload.stepStatus -eq "FAILED" })
$WarningSteps = @($PriorStepEvents | Where-Object { $_.payload.stepStatus -eq "WARNING" })
$DeploymentStatus = if ($FailedSteps.Count -gt 0) {
    "FAILED"
} elseif ($WarningSteps.Count -gt 0) {
    "WARNING"
} else {
    "SUCCESS"
}
$DeploymentErrorCode = if ($FailedSteps.Count -gt 0) {
    "DEPLOYMENT_STEPS_FAILED"
} elseif ($WarningSteps.Count -gt 0) {
    "DEPLOYMENT_STEPS_WITH_WARNINGS"
} else {
    $null
}
$DeploymentSummary = "Status=$DeploymentStatus; FailedSteps=$($FailedSteps.Count); WarningSteps=$($WarningSteps.Count); TotalSteps=$($PriorStepEvents.Count)"
Write-Log "Finalizacion de despliegue: $DeploymentSummary" $(if ($DeploymentStatus -eq "SUCCESS") { "OK" } elseif ($DeploymentStatus -eq "WARNING") { "WARN" } else { "ERROR" })
Write-BlueDeployStep -StepNumber "999" `
                    -StepName "DEPLOYMENT_COMPLETED" `
                    -StepStatus $DeploymentStatus `
                    -Message "La fase de postinstalación del despliegue alcanzó la finalización. $DeploymentSummary" `
                    -ErrorCode $DeploymentErrorCode `
                    -RawOutput $DeploymentSummary

# -----------------------------------------------------------------------
# 6. CIERRE, ENVÍO DE EVIDENCIAS Y LIMPIEZA
# -----------------------------------------------------------------------

$MsgLog = "¿Desea leer los logs de instalación de OSD antes de continuar?"
$RespLog = [System.Windows.MessageBox]::Show($MsgLog, "Logs de OSD", "YesNo", "Question")
if ($RespLog -eq "Yes") {
    if (Test-Path "C:\Windows\Panther\unattendGC\msgina.log") { Start-Process notepad.exe "C:\Windows\Panther\unattendGC\msgina.log" -Wait }
    if (Test-Path $LogFile) { Start-Process notepad.exe $LogFile -Wait }
}

$MsgDel = "¿Desea borrar la carpeta C:\OSDCloud y cerrar sesión?"
$RespDel = [System.Windows.MessageBox]::Show($MsgDel, "Limpieza Final", "YesNo", "Warning")

if ($RespDel -eq "Yes") {
    Write-BlueDeployStep -StepNumber "140" -StepName "CLEANUP" -StepStatus "SUCCESS" -Message "Limpieza y cierre de sesión iniciado"
    
    # Desactivar Autologon
    Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Winlogon" -Name "AutoAdminLogon" -Value "0" -Force
    schtasks /delete /tn "InstallSoftware" /f 2>$null

    # Enviar evidencias antes de borrar todo
    $ApiUrl = if ($env:BLUEDEPLOY_API_URL) { $env:BLUEDEPLOY_API_URL } else { "http://178.105.231.10" }
    $ApiKey = if ($env:BLUEDEPLOY_API_KEY) { $env:BLUEDEPLOY_API_KEY } else { "bluedeploy-secret-key-change-in-prod" }

    if (Test-Path "C:\Scripts\Security\Check-SecurityCompliance.ps1") {
        powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\Scripts\Security\Check-SecurityCompliance.ps1"
    } elseif (Test-Path "C:\OSDCloud\Scripts\Security\Check-SecurityCompliance.ps1") {
        powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\OSDCloud\Scripts\Security\Check-SecurityCompliance.ps1"
    }

    if (Test-Path "C:\tmp\BlueDeploy\Send-BlueDeployQueue.ps1") {
        powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\tmp\BlueDeploy\Send-BlueDeployQueue.ps1" -QueuePath "C:\tmp\BlueDeploy\Queue" -ApiUrl $ApiUrl -ApiKey $ApiKey -ContinueOnError
    } elseif (Test-Path "C:\OSDCloud\Scripts\BlueDeploy\Send-BlueDeployQueue.ps1") {
        powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\OSDCloud\Scripts\BlueDeploy\Send-BlueDeployQueue.ps1" -QueuePath "C:\tmp\BlueDeploy\Queue" -ApiUrl $ApiUrl -ApiKey $ApiKey -ContinueOnError
    }

    if (Test-Path "C:\OSDCloud") {
        $CleanupCmd = "Start-Sleep -Seconds 2; Remove-Item -Path 'C:\OSDCloud' -Recurse -Force"
        Start-Process powershell.exe -ArgumentList "-NoProfile", "-Command", $CleanupCmd -WindowStyle Hidden
    }

    Write-Host "Cerrando sesión en 5 segundos..." -ForegroundColor Cyan
    Start-Sleep -Seconds 5
    & shutdown.exe /l /f
}
