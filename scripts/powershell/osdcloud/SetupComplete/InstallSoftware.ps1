# ============================================================
#  InstallSoftware.ps1 - OSD-BlueDeploy Deployment
#  Se ejecuta tras las políticas de grupo en el último paso.
#  Instala el software corporativo embebido en la imagen.
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
$DeploymentId = "BLUE-DEPLOY-$Hostname-$(Get-Date -Format 'yyyyMMdd')"
$LogPath = "C:\OSDCloud\Logs"
$LogFile = "$LogPath\InstallSoftware.log"

function Write-BlueDeployStep {
    param ([string]$StepNumber, [string]$StepName, [string]$StepStatus, [string]$Message)
    $EventData = [ordered]@{
        "eventType" = "DEPLOYMENT_STEP"; "deploymentId" = $DeploymentId; "createdAt" = (Get-Date -Format "yyyy-MM-ddTHH:mm:ss")
        "sourceScript" = "InstallSoftware.ps1"; "payload" = @{ "stepName" = $StepName; "stepStatus" = $StepStatus; "message" = $Message }
    }
    $JsonPath = Join-Path $QueueFolder ("{0}_{1}_step.json" -f $StepNumber, ($StepName.ToLower()))
    $EventData | ConvertTo-Json -Depth 5 | Out-File -FilePath $JsonPath -Encoding utf8 -Force
}

# -----------------------------------------------------------------------
# 1. BÚSQUEDA DE APPS
# -----------------------------------------------------------------------
$AppsSource = "C:\OSDCloud\Apps"
if (-not (Test-Path $AppsSource)) {
    foreach ($Drive in @('D','E','F','G','H')) {
        if (Test-Path "${Drive}:\OSDCloud\Apps") { $AppsSource = "${Drive}:\OSDCloud\Apps"; break }
    }
}

$AppList = @()
if (Test-Path $AppsSource) {
    foreach ($Folder in (Get-ChildItem -Path $AppsSource -Directory)) {
        $JsonPath = Join-Path $Folder.FullName "app.json"
        if (Test-Path $JsonPath) {
            $Json = Get-Content $JsonPath -Raw | ConvertFrom-Json
            $AppList += [PSCustomObject]@{ Name = $Json.Name; Obligatoria = [bool]($Json.Obligatoria); Directorio = $Folder.FullName; Instalador = Join-Path $Folder.FullName $Json.Instalador; Argumentos = $Json.Argumentos }
        }
    }
}

# -----------------------------------------------------------------------
# 2. INTERFAZ WPF
# -----------------------------------------------------------------------
$XamlText = @"
<Window xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation" Title="OSD-BlueDeploy - Gestión de Software" Width="540" Height="620" WindowStartupLocation="CenterScreen" ResizeMode="NoResize" Background="#F4F4F4">
    <Grid Margin="20">
        <Grid.RowDefinitions><RowDefinition Height="Auto"/><RowDefinition Height="*"/><RowDefinition Height="Auto"/></Grid.RowDefinitions>
        <StackPanel Grid.Row="0" Margin="0,0,0,10">
            <TextBlock Text="Instalación de Software Corporativo" FontSize="18" FontWeight="Bold"/>
            <TextBlock x:Name="SubTitle" Text="Seleccione las aplicaciones o elija Omitir." Foreground="#555"/>
        </StackPanel>
        <Grid x:Name="PanelSel" Grid.Row="1">
            <Grid.RowDefinitions><RowDefinition Height="*"/><RowDefinition Height="Auto"/></Grid.RowDefinitions>
            <Border Background="White" BorderBrush="#DADADA" BorderThickness="1"><ScrollViewer><StackPanel x:Name="AppPanel" Margin="10"/></ScrollViewer></Border>
            <StackPanel Grid.Row="1" Orientation="Horizontal" Margin="0,10"><Button x:Name="BtnAll" Content="Todas" Width="80" Margin="0,0,5,0"/><Button x:Name="BtnNone" Content="Ninguna" Width="80"/></StackPanel>
        </Grid>
        <Border x:Name="PanelProg" Grid.Row="1" Visibility="Collapsed" Background="White" BorderBrush="#DADADA" BorderThickness="1">
            <ScrollViewer><StackPanel x:Name="ProgPanel" Margin="10"/></ScrollViewer>
        </Border>
        <UniformGrid Grid.Row="2" Columns="2" Margin="0,10,0,0">
            <Button x:Name="BtnSkip" Content="Omitir e Ir a Limpieza" Height="40" Margin="0,0,5,0" Background="#9E9E9E" Foreground="White"/>
            <Button x:Name="BtnInstall" Content="Instalar" Height="40" Background="#1A73E8" Foreground="White" FontWeight="Bold"/>
        </UniformGrid>
    </Grid>
</Window>
"@

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
# 3. ACCIONES
# -----------------------------------------------------------------------

# Botón Omitir
$BtnSkip.Add_Click({ $Window.Close() })

# Botón Instalar
$BtnInstall.Add_Click({
    if ($BtnInstall.Content -eq "Finalizar") { $Window.Close(); return }

    $SelectedApps = $AppList | Where-Object { $Checkboxes[$_.Name].IsChecked }
    if ($SelectedApps.Count -eq 0) { [System.Windows.MessageBox]::Show("No hay apps seleccionadas."); return }

    $PanelSel.Visibility = "Collapsed"; $PanelProg.Visibility = "Visible"
    $BtnInstall.IsEnabled = $false; $BtnSkip.IsEnabled = $false
    $SubTitle.Text = "Instalando aplicaciones..."

    $ProgRows = @{}
    foreach ($App in $SelectedApps) {
        $Lab = New-Object System.Windows.Controls.TextBlock; $Lab.Text = "• $($App.Name): Esperando..."; $ProgPanel.Children.Add($Lab) | Out-Null
        $ProgRows[$App.Name] = $Lab
    }

    $Sync = [hashtable]::Synchronized(@{ Window=$Window; Apps=$SelectedApps; Rows=$ProgRows; Btn=$BtnInstall; Sub=$SubTitle })
    $Runspace = [runspacefactory]::CreateRunspace(); $Runspace.ApartmentState = "STA"; $Runspace.Open()
    $Runspace.SessionStateProxy.SetVariable("Sync", $Sync)

    $Script = {
        foreach ($App in $Sync.Apps) {
            $Sync.Window.Dispatcher.Invoke([action]{ $Sync.Rows[$App.Name].Text = "• $($App.Name): Instalando..."; $Sync.Rows[$App.Name].Foreground = "Blue" })
            $ExitCode = -1
            try {
                $Ext = [System.IO.Path]::GetExtension($App.Instalador).ToLower()
                if ($Ext -eq ".msi") {
                    $Proc = Start-Process "msiexec" -ArgumentList "/i `"$($App.Instalador)`" /qn /norestart $($App.Argumentos)" -Wait -PassThru
                } else {
                    $Proc = Start-Process $App.Instalador -ArgumentList $App.Argumentos -Wait -PassThru -WorkingDirectory $App.Directorio
                }
                $ExitCode = $Proc.ExitCode
            } catch {}
            $Status = "Error"; $Col = "Red"; if ($ExitCode -eq 0 -or $ExitCode -eq 3010) { $Status = "OK"; $Col = "Green" }
            $Sync.Window.Dispatcher.Invoke([action]{ $Sync.Rows[$App.Name].Text = "• $($App.Name): $Status ($ExitCode)"; $Sync.Rows[$App.Name].Foreground = $Col })
        }
        $Sync.Window.Dispatcher.Invoke([action]{ $Sync.Btn.Content = "Finalizar"; $Sync.Btn.IsEnabled = $true; $Sync.Sub.Text = "Instalación completada."; $Sync.Btn.Background = "SeaGreen" })
    }
    $PS = [powershell]::Create().AddScript($Script); $PS.Runspace = $Runspace; $PS.BeginInvoke()
})

$Window.ShowDialog() | Out-Null

# -----------------------------------------------------------------------
# 4. FASE DE LOGS Y LIMPIEZA (SIEMPRE SE EJECUTA AL CERRAR LA UI)
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
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\Scripts\Security\Check-SecurityCompliance.ps1"
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "C:\tmp\BlueDeploy\Send-BlueDeployQueue.ps1" -QueuePath "C:\tmp\BlueDeploy\Queue" -ApiUrl "http://192.168.18.2:8080" -ContinueOnError

    if (Test-Path "C:\OSDCloud") {
        $CleanupCmd = "Start-Sleep -Seconds 2; Remove-Item -Path 'C:\OSDCloud' -Recurse -Force"
        Start-Process powershell.exe -ArgumentList "-NoProfile", "-Command", $CleanupCmd -WindowStyle Hidden
    }

    Write-Host "Cerrando sesión en 5 segundos..." -ForegroundColor Cyan
    Start-Sleep -Seconds 5
    & shutdown.exe /l /f
}