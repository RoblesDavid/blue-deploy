# ============================================================
#  01 - Start-OSDCloud.ps1 - BlueDeploy Deployment
#  Version 2.0 - Despliegue Automatizado y Telemetría BlueDeploy
# ============================================================

param(
    [string]$BlueDeployModulePath = "G:\OSDCloud\Config\Scripts\BlueDeploy\BlueDeployDeploymentJson.ps1"
)

# -----------------------------------------------------------------------
# SECCION 0: INICIALIZACION Y VALIDACION DE RUTAS
# -----------------------------------------------------------------------

# Codificacion de la contrasena en base64 (Entorno de Laboratorio / QEMU)
$PasswordEncoded = "dGVzdDEyMw=="
$PasswordDecoded = [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($PasswordEncoded))

# Determinacion de ruta de logs: En WinPE usa X:, en SO normal usa C:
$IsWinPE = (Test-Path "X:\") -and (-not (Test-Path "C:\Windows"))
$LogDrive = if ($IsWinPE) { "X:" } else { "C:" }
$LogPath = "$LogDrive\OSDCloud\Logs"

# Crear directorio de logs si no existe
if (-not (Test-Path $LogPath)) {
    New-Item -Path $LogPath -ItemType Directory -Force | Out-Null
}

# Archivo de log consolidado
$LogFile = "$LogPath\Deploy-$(Get-Date -Format 'yyyy-MM-dd_HHmmss').log"

# Funcion para escribir logs
function Write-Log {
    param(
        [string]$Mensaje, 
        [string]$Nivel = "INFO"
    )
    $Linea = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [$Nivel] $Mensaje"
    $Linea | Out-File -FilePath $LogFile -Append -Encoding utf8
    Write-Host $Linea -ForegroundColor $(switch ($Nivel) {
        "ERROR" { "Red" }
        "WARN"  { "Yellow" }
        "OK"    { "Green" }
        default { "White" }
    })
}

Write-Log "=== INICIO DEL SCRIPT DE DESPLIEGUE BLUEDEPLOY ===" "INFO"
Write-Log "Entorno: $(if ($IsWinPE) { 'WinPE' } else { 'Windows Instalado' })" "INFO"
Write-Log "Ruta de logs: $LogPath" "INFO"

# -----------------------------------------------------------------------
# SECCION 1: VALIDACION DE MODULO BLUEDEPLOY
# -----------------------------------------------------------------------

$BlueDeployModuleLoaded = $false

Write-Log "Intentando cargar modulo BlueDeployJson desde: $BlueDeployModulePath" "INFO"

if (Test-Path $BlueDeployModulePath) {
    try {
        . $BlueDeployModulePath
        Write-Log "Modulo BlueDeployJson cargado exitosamente" "OK"
        $BlueDeployModuleLoaded = $true
    } catch {
        Write-Log "Error al cargar modulo BlueDeployJson: $_" "WARN"
        Write-Log "Se crearan funciones fallback inline" "WARN"
    }
} else {
    Write-Log "Modulo BlueDeployJson no encontrado en $BlueDeployModulePath" "WARN"
    Write-Log "Se crearan funciones fallback inline" "WARN"
}

# -----------------------------------------------------------------------
# SECCION 2: FUNCIONES FALLBACK PARA BLUEDEPLOY
# -----------------------------------------------------------------------

if (-not $BlueDeployModuleLoaded) {
    Write-Log "Creando funciones fallback para generacion de JSON" "INFO"

    function New-BlueDeployDeploymentId {
        param([string]$Hostname)
        $CurrentDate = Get-Date -Format "yyyyMMdd"
        return "DEPLOY-LAB-$Hostname-$CurrentDate"
    }

    function New-BlueDeployDeploymentJson {
        param(
            [string]$DeploymentId,
            [string]$Hostname,
            [string]$DeviceModel,
            [string]$DomainName,
            [string]$QueuePath,
            [string]$FileName = "000_deployment.json",
            [string]$SourceScript = "01-Start-OSDCloud.ps1"
        )

        $Timestamp = Get-Date -Format "yyyy-MM-ddTHH:mm:ss"
        $JsonPath = Join-Path $QueuePath $FileName

        $EventData = [ordered]@{
            "eventType"    = "INITIALIZE_DEPLOYMENT"
            "deploymentId" = $DeploymentId
            "apiPath"      = "/api/deployments"
            "createdAt"    = $Timestamp
            "sourceScript" = $SourceScript
            "payload"      = @{
                "deploymentId" = $DeploymentId
                "hostname"     = $Hostname
                "deviceModel"  = $DeviceModel
                "domainName"   = $DomainName
                "status"       = "STARTING"
                "errorCode"    = $null
            }
        }

        try {
            $EventData | ConvertTo-Json -Depth 10 | Out-File -FilePath $JsonPath -Encoding utf8 -Force
            Write-Log "JSON de despliegue generado: $FileName" "OK"
        } catch {
            Write-Log "Error generando JSON ($FileName): $_" "WARN"
        }
    }

    function New-BlueDeployStepJson {
        param(
            [string]$DeploymentId,
            [string]$StepName,
            [string]$StepStatus,
            [string]$Message,
            [string]$ErrorCode = $null,
            [string]$RawOutput = "",
            [string]$QueuePath,
            [string]$FileName,
            [string]$SourceScript = "01-Start-OSDCloud.ps1"
        )

        $Timestamp = Get-Date -Format "yyyy-MM-ddTHH:mm:ss"
        $JsonPath = Join-Path $QueuePath $FileName

        $EventData = [ordered]@{
            "eventType"    = "DEPLOYMENT_STEP"
            "deploymentId" = $DeploymentId
            "apiPath"      = "/api/deployments/$DeploymentId/steps"
            "createdAt"    = $Timestamp
            "sourceScript" = $SourceScript
            "payload"      = @{
                "stepName"     = $StepName
                "stepStatus"   = $StepStatus
                "message"      = $Message
                "errorCode"    = $ErrorCode
                "rawOutput"    = $RawOutput
            }
        }

        try {
            $EventData | ConvertTo-Json -Depth 10 | Out-File -FilePath $JsonPath -Encoding utf8 -Force
            Write-Log "Paso JSON generado: $FileName" "OK"
        } catch {
            Write-Log "Error generando paso JSON ($FileName): $_" "WARN"
        }
    }
}

# -----------------------------------------------------------------------
# SECCION 3: INTERFAZ GRAFICA
# -----------------------------------------------------------------------

Write-Log "Cargando ensamblados de interfaz grafica..." "INFO"

try {
    Add-Type -AssemblyName System.Windows.Forms
    Add-Type -AssemblyName System.Drawing
    Write-Log "Ensamblados de interfaz cargados correctamente" "OK"
} catch {
    Write-Log "Error cargando ensamblados: $_" "ERROR"
    exit
}

Write-Log "Mostrando formulario de configuracion..." "INFO"

# Creacion del Formulario
$Form = New-Object System.Windows.Forms.Form
$Form.Text = "Configuracion BlueDeploy Lab"
$Form.Size = New-Object System.Drawing.Size(400, 490)
$Form.StartPosition = "CenterScreen"
$Form.Topmost = $true
$Form.BackColor = [System.Drawing.Color]::White

# Etiqueta: Nombre del Equipo
$LblNombre = New-Object System.Windows.Forms.Label
$LblNombre.Text = "Nombre del Equipo:"
$LblNombre.Location = New-Object System.Drawing.Point(20, 20)
$LblNombre.AutoSize = $true
$Form.Controls.Add($LblNombre)

$TxtNombre = New-Object System.Windows.Forms.TextBox
$TxtNombre.Location = New-Object System.Drawing.Point(20, 45)
$TxtNombre.Size = New-Object System.Drawing.Size(340, 20)
$TxtNombre.Text = "LAB-PC01"
$Form.Controls.Add($TxtNombre)

# Etiqueta: Descripcion del equipo
$LblDescripcion = New-Object System.Windows.Forms.Label
$LblDescripcion.Text = "Descripcion del equipo:"
$LblDescripcion.Location = New-Object System.Drawing.Point(20, 80)
$LblDescripcion.AutoSize = $true
$Form.Controls.Add($LblDescripcion)

$TxtDescripcion = New-Object System.Windows.Forms.TextBox
$TxtDescripcion.Location = New-Object System.Drawing.Point(20, 105)
$TxtDescripcion.Size = New-Object System.Drawing.Size(340, 20)
$TxtDescripcion.Text = "Puesto de trabajo Laboratorio"
$Form.Controls.Add($TxtDescripcion)

# Etiqueta: Codigo CEGES / Inventario
$LblCodigoCeges = New-Object System.Windows.Forms.Label
$LblCodigoCeges.Text = "Codigo CEGES / Inventario:"
$LblCodigoCeges.Location = New-Object System.Drawing.Point(20, 140)
$LblCodigoCeges.AutoSize = $true
$Form.Controls.Add($LblCodigoCeges)

$TxtCodigoCeges = New-Object System.Windows.Forms.TextBox
$TxtCodigoCeges.Location = New-Object System.Drawing.Point(20, 165)
$TxtCodigoCeges.Size = New-Object System.Drawing.Size(340, 20)
$TxtCodigoCeges.Text = "LAB-INV-001"
$Form.Controls.Add($TxtCodigoCeges)

# Etiqueta: Sistema Operativo
$LblSO = New-Object System.Windows.Forms.Label
$LblSO.Text = "Sistema Operativo a instalar:"
$LblSO.Location = New-Object System.Drawing.Point(20, 200)
$LblSO.AutoSize = $true
$Form.Controls.Add($LblSO)

$CmbSO = New-Object System.Windows.Forms.ComboBox
$CmbSO.Location = New-Object System.Drawing.Point(20, 225)
$CmbSO.Size = New-Object System.Drawing.Size(340, 20)
$CmbSO.DropDownStyle = [System.Windows.Forms.ComboBoxStyle]::DropDownList
$CmbSO.Items.Add("1. Windows 11 Pro") | Out-Null
$CmbSO.SelectedIndex = 0
$Form.Controls.Add($CmbSO)

# Checkbox: Confirmacion de Particiones
$ChkPart = New-Object System.Windows.Forms.CheckBox
$ChkPart.Text = "Confirmo el borrado de particiones y disco"
$ChkPart.Location = New-Object System.Drawing.Point(20, 275)
$ChkPart.Size = New-Object System.Drawing.Size(340, 30)
$ChkPart.ForeColor = [System.Drawing.Color]::Red
$Form.Controls.Add($ChkPart)

# Boton: Iniciar
$BtnStart = New-Object System.Windows.Forms.Button
$BtnStart.Text = "INICIAR DESPLIEGUE"
$BtnStart.Location = New-Object System.Drawing.Point(100, 365)
$BtnStart.Size = New-Object System.Drawing.Size(180, 40)
$BtnStart.DialogResult = [System.Windows.Forms.DialogResult]::OK
$Form.Controls.Add($BtnStart)

# Mostrar formulario y capturar resultado
$DialogResult = $Form.ShowDialog()

if ($DialogResult -ne [System.Windows.Forms.DialogResult]::OK) {
    Write-Log "Despliegue cancelado por el usuario." "WARN"
    exit
}

if ($ChkPart.Checked -eq $false) {
    Write-Log "Particiones no aceptadas. Cancelando despliegue." "WARN"
    exit
}

# Capturar valores del formulario
$NombreEquipo = $TxtNombre.Text.ToUpper()
$DescripcionEquipo = $TxtDescripcion.Text.Trim()
$CodigoCeges = $TxtCodigoCeges.Text.Trim()

if ([string]::IsNullOrWhiteSpace($DescripcionEquipo) -or [string]::IsNullOrWhiteSpace($CodigoCeges)) {
    [System.Windows.Forms.MessageBox]::Show(
        "Debe indicar la descripcion del equipo y el codigo CEGES / Inventario.",
        "Datos incompletos",
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Warning
    ) | Out-Null
    exit
}

$WimNumero = ($CmbSO.SelectedIndex + 1).ToString()

# Generar DeploymentId
$DeploymentId = New-BlueDeployDeploymentId -Hostname $NombreEquipo
Write-Log "Configuracion aceptada: Equipo=$NombreEquipo, WIM_Index=$WimNumero, DeploymentId=$DeploymentId" "OK"

# -----------------------------------------------------------------------
# SECCION 4: VALIDACION DE ESTRUCTURA DE CARPETAS
# -----------------------------------------------------------------------

Write-Log "Validando estructura de carpetas en USB/Medio de instalacion..." "INFO"

$ScriptDrives = Get-PSDrive -PSProvider FileSystem | Where-Object { $_.Name -ne 'X' -and $_.Name -ne 'C' }
$UsbRoot = $null
foreach ($Drive in $ScriptDrives) {
    if (Test-Path "$($Drive.Root)OSDCloud") {
        $UsbRoot = $Drive.Root
        break
    }
}
if (-not $UsbRoot) { $UsbRoot = "G:\" }

$RequiredFolders = @(
    (Join-Path $UsbRoot "OSDCloud\Scripts\SetupComplete"),
    (Join-Path $UsbRoot "OSDCloud\Config\Scripts\BlueDeploy")
)

$MissingFolders = @()
foreach ($Folder in $RequiredFolders) {
    if (-not (Test-Path $Folder)) {
        $MissingFolders += $Folder
        Write-Log "[WARNING] Carpeta no encontrada: $Folder" "WARN"
    } else {
        Write-Log "[OK] Carpeta validada: $Folder" "OK"
    }
}

if ($MissingFolders.Count -gt 0) {
    Write-Log "Se encontraron $($MissingFolders.Count) carpetas faltantes en la estructura USB." "WARN"
    foreach ($Folder in $MissingFolders) {
        Write-Log "  - $Folder" "WARN"
    }
    Write-Log "Continuando de todas formas..." "WARN"
}

# -----------------------------------------------------------------------
# SECCION 5: PREPARACION DE COLA LOCAL Y JSON INICIAL
# -----------------------------------------------------------------------

# Identificar unidad de destino
if ($IsWinPE) {
    $TargetDrive = "C:"
    Write-Log "Entorno WinPE detectado. TargetDrive temporal: $TargetDrive" "INFO"
} else {
    $TargetDriveLetter = (Get-Volume | Where-Object { $_.FileSystemLabel -eq 'Windows' }).DriveLetter
    if (-not $TargetDriveLetter) {
        $TargetDriveLetter = "C"
        Write-Log "No se encontro etiqueta 'Windows', asumiendo C: por defecto." "WARN"
    }
    $TargetDrive = "${TargetDriveLetter}:"
}

Write-Log "Unidad de destino identificada: $TargetDrive" "INFO"

# Crear estructura de directorios
$QueueFolder = "$TargetDrive\tmp\BlueDeploy\Queue"
$OSDCloudFolder = "$TargetDrive\OSDCloud"

try {
    if (-not (Test-Path $OSDCloudFolder)) { New-Item -ItemType Directory -Path $OSDCloudFolder -Force | Out-Null }
    if (-not (Test-Path $QueueFolder)) { New-Item -ItemType Directory -Path $QueueFolder -Force | Out-Null }
    Write-Log "Estructura de directorios creada en $TargetDrive" "OK"
} catch {
    Write-Log "Error creando directorios: $_" "WARN"
}

# Guardar informacion de identidad (en UTF-8 limpio sin BOM para evitar fallos de lectura en PowerShell)
try {
    [System.IO.File]::WriteAllText("$OSDCloudFolder\ComputerName.txt", $NombreEquipo, [System.Text.Encoding]::ASCII)
    [System.IO.File]::WriteAllText("$OSDCloudFolder\DeploymentId.txt", $DeploymentId, [System.Text.Encoding]::ASCII)
    $DatosEquipoContent = @("Descripcion=$DescripcionEquipo", "CodigoCEGES=$CodigoCeges")
    [System.IO.File]::WriteAllLines("$OSDCloudFolder\DatosEquipo.txt", $DatosEquipoContent, (New-Object System.Text.UTF8Encoding($false)))
    Write-Log "Identidad, descripcion y codigo CEGES guardados" "OK"
} catch {
    Write-Log "Error guardando los datos del equipo: $_" "ERROR"
    throw
}

# Generar JSON 000 de inicializacion
Write-Log "Generando JSON de inicializacion (paso 000)..." "INFO"
New-BlueDeployDeploymentJson `
    -DeploymentId $DeploymentId `
    -Hostname $NombreEquipo `
    -DeviceModel "UNKNOWN_WINPE" `
    -DomainName "lab.local" `
    -QueuePath $QueueFolder `
    -FileName "000_deployment.json" `
    -SourceScript "01-Start-OSDCloud.ps1"

# -----------------------------------------------------------------------
# SECCION 6: EJECUCION DE START-OSDCLOUD
# -----------------------------------------------------------------------

Write-Log "====== INICIANDO START-OSDCLOUD ======" "INFO"

$OSDCommand = "Start-OSDCloud -FindImageFile -OSImageIndex $WimNumero -SkipAutopilot -SkipODT -ZTI"

try {
    Write-Log "Ejecutando: $OSDCommand" "INFO"
    "1" | powershell.exe -ExecutionPolicy Bypass -NoProfile -Command $OSDCommand
    Write-Log "Start-OSDCloud completado exitosamente" "OK"
} catch {
    Write-Log "Error ejecutando Start-OSDCloud: $_" "ERROR"
    New-BlueDeployStepJson `
        -DeploymentId $DeploymentId `
        -StepName "OSDCLOUD_EXECUTION" `
        -StepStatus "FAILED" `
        -Message "Error ejecutando Start-OSDCloud" `
        -ErrorCode "OSDCLOUD_ERROR" `
        -RawOutput "$_" `
        -QueuePath $QueueFolder `
        -FileName "015_osdcloud_error.json" `
        -SourceScript "01-Start-OSDCloud.ps1"
}

Write-Log "====== FINALIZANDO START-OSDCLOUD ======" "INFO"

# -----------------------------------------------------------------------
# SECCION 7: POST-CONFIGURACION
# -----------------------------------------------------------------------

Write-Log "Iniciando fase de post-configuracion..." "INFO"

# Esperar a que se asienten las letras de unidad
Start-Sleep -Seconds 5

# Detectar la unidad donde se instalo Windows
if (-not $IsWinPE) {
    $TargetDriveLetter = (Get-Volume | Where-Object { $_.FileSystemLabel -eq 'Windows' }).DriveLetter
    if (-not $TargetDriveLetter) {
        $TargetDriveLetter = "C"
        Write-Log "No se encontro etiqueta 'Windows', asumiendo C: por defecto." "WARN"
    }
    $TargetDrive = "${TargetDriveLetter}:"
    Write-Log "Unidad de destino actualizada: $TargetDrive" "INFO"
}

# 1. Crear estructura de directorios finales
$DestLogPath = "$TargetDrive\OSDCloud\Logs"
$BlueDeployStatePath = "$TargetDrive\OSDCloud"
$FinalBlueDeployQueuePath = "$TargetDrive\tmp\BlueDeploy\Queue"

try {
    if (-not (Test-Path $DestLogPath)) { New-Item -Path $DestLogPath -ItemType Directory -Force | Out-Null }
    if (-not (Test-Path $FinalBlueDeployQueuePath)) { New-Item -Path $FinalBlueDeployQueuePath -ItemType Directory -Force | Out-Null }
    Write-Log "Directorios finales creados en $TargetDrive" "OK"
} catch {
    Write-Log "Error creando directorios finales: $_" "WARN"
}

# 2. Guardar informacion final sin BOM
try {
    [System.IO.File]::WriteAllText("$TargetDrive\OSDCloud\ComputerName.txt", $NombreEquipo, [System.Text.Encoding]::ASCII)
    [System.IO.File]::WriteAllText("$BlueDeployStatePath\DeploymentId.txt", $DeploymentId, [System.Text.Encoding]::ASCII)
    $DatosEquipoContent = @("Descripcion=$DescripcionEquipo", "CodigoCEGES=$CodigoCeges")
    [System.IO.File]::WriteAllLines("$BlueDeployStatePath\DatosEquipo.txt", $DatosEquipoContent, (New-Object System.Text.UTF8Encoding($false)))
    Write-Log "Informacion de identidad guardada en destino" "OK"
} catch {
    Write-Log "Error guardando informacion en destino: $_" "WARN"
}

# 3. Generar JSON de paso: Hostname capturado
Write-Log "Generando JSON de paso 010 (hostname capturado)..." "INFO"
New-BlueDeployStepJson `
    -DeploymentId $DeploymentId `
    -StepName "HOSTNAME_CAPTURED" `
    -StepStatus "SUCCESS" `
    -Message "Hostname capturado desde entrada de tecnico y guardado en archivo de texto" `
    -RawOutput "Hostname=$NombreEquipo" `
    -QueuePath $FinalBlueDeployQueuePath `
    -FileName "010_hostname_captured_step.json" `
    -SourceScript "01-Start-OSDCloud.ps1"

# 4. Generar JSON de paso: Windows instalado
Write-Log "Generando JSON de paso 020 (Windows instalado)..." "INFO"
New-BlueDeployStepJson `
    -DeploymentId $DeploymentId `
    -StepName "WINDOWS_INSTALLATION" `
    -StepStatus "SUCCESS" `
    -Message "Imagen de Windows 11 aplicada exitosamente usando motor OSDCloud" `
    -RawOutput "OSImageIndex=$WimNumero; TargetDrive=$TargetDrive" `
    -QueuePath $FinalBlueDeployQueuePath `
    -FileName "020_windows_installation_step.json" `
    -SourceScript "01-Start-OSDCloud.ps1"

# -----------------------------------------------------------------------
# SECCION 8: CONFIGURACION DE UNATTEND.XML
# -----------------------------------------------------------------------

Write-Log "Creando archivo Unattend.xml..." "INFO"

$UnattendFolder = "$TargetDrive\Windows\Panther"
if (-not (Test-Path $UnattendFolder)) {
    New-Item -Path $UnattendFolder -ItemType Directory -Force | Out-Null
}

$UnattendXml = @"
<?xml version="1.0" encoding="utf-8"?>
<unattend xmlns="urn:schemas-microsoft-com:unattend"
          xmlns:wcm="http://schemas.microsoft.com/WMIConfig/2002/State"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  <!-- ========================= -->
  <!-- PASS: windowsPE          -->
  <!-- ========================= -->
  <settings pass="windowsPE">
    <component name="Microsoft-Windows-International-Core-WinPE"
               processorArchitecture="amd64"
               publicKeyToken="31bf3856ad364e35"
               language="neutral"
               versionScope="nonSxS">
      <SetupUILanguage>
        <UILanguage>es-ES</UILanguage>
      </SetupUILanguage>
      <InputLocale>es-ES</InputLocale>
      <SystemLocale>es-ES</SystemLocale>
      <UILanguage>es-ES</UILanguage>
      <UILanguageFallback>en-US</UILanguageFallback>
      <UserLocale>es-ES</UserLocale>
    </component>

    <component name="Microsoft-Windows-Setup"
               processorArchitecture="amd64"
               publicKeyToken="31bf3856ad364e35"
               language="neutral"
               versionScope="nonSxS">
      <UserData>
        <AcceptEula>true</AcceptEula>
      </UserData>
    </component>
  </settings>

  <!-- ========================= -->
  <!-- PASS: specialize          -->
  <!-- ========================= -->
  <settings pass="specialize">
    <component name="Microsoft-Windows-Shell-Setup"
               processorArchitecture="amd64"
               publicKeyToken="31bf3856ad364e35"
               language="neutral"
               versionScope="nonSxS">
      <ComputerName>$NombreEquipo</ComputerName>
      <TimeZone>Romance Standard Time</TimeZone>
    </component>

    <component name="Microsoft-Windows-WindowsUpdate-AU"
               processorArchitecture="amd64"
               publicKeyToken="31bf3856ad364e35"
               language="neutral"
               versionScope="nonSxS">
      <NoAutoUpdate>true</NoAutoUpdate>
      <AUOptions>1</AUOptions>
    </component>
  </settings>

  <!-- ========================= -->
  <!-- PASS: oobeSystem          -->
  <!-- ========================= -->
  <settings pass="oobeSystem">

    <component name="Microsoft-Windows-International-Core"
               processorArchitecture="amd64"
               publicKeyToken="31bf3856ad364e35"
               language="neutral"
               versionScope="nonSxS">
      <InputLocale>es-ES</InputLocale>
      <SystemLocale>es-ES</SystemLocale>
      <UILanguage>es-ES</UILanguage>
      <UILanguageFallback>en-US</UILanguageFallback>
      <UserLocale>es-ES</UserLocale>
    </component>

    <component name="Microsoft-Windows-Shell-Setup"
               processorArchitecture="amd64"
               publicKeyToken="31bf3856ad364e35"
               language="neutral"
               versionScope="nonSxS">

      <OOBE>
        <HideEULAPage>true</HideEULAPage>
        <HideOEMRegistrationScreen>true</HideOEMRegistrationScreen>
        <HideOnlineAccountScreens>true</HideOnlineAccountScreens>
        <HideWirelessSetupInOOBE>true</HideWirelessSetupInOOBE>
        <HideLocalAccountScreen>true</HideLocalAccountScreen>
        <SkipMachineOOBE>true</SkipMachineOOBE>
        <SkipUserOOBE>true</SkipUserOOBE>
        <NetworkLocation>Work</NetworkLocation>
        <ProtectYourPC>3</ProtectYourPC>
      </OOBE>

      <!-- Cuenta local -->
      <UserAccounts>
        <LocalAccounts>
          <LocalAccount wcm:action="add">
            <Name>Administrador</Name>
            <Group>Administrators</Group>
            <Password>
              <Value>$PasswordDecoded</Value>
              <PlainText>true</PlainText>
            </Password>
          </LocalAccount>
        </LocalAccounts>
      </UserAccounts>

      <!-- AutoLogon -->
      <AutoLogon>
        <Enabled>true</Enabled>
        <LogonCount>3</LogonCount>
        <Username>Administrador</Username>
        <Domain>.</Domain>
        <Password>
          <Value>$PasswordDecoded</Value>
          <PlainText>true</PlainText>
        </Password>
      </AutoLogon>

      <TimeZone>Romance Standard Time</TimeZone>
    </component>
  </settings>

</unattend>
"@

try {
    # Guardar en UTF-8 limpio sin BOM
    [System.IO.File]::WriteAllText("$UnattendFolder\unattend.xml", $UnattendXml, (New-Object System.Text.UTF8Encoding($false)))
    Write-Log "Unattend.xml creado correctamente (UTF-8 limpio)" "OK"
} catch {
    Write-Log "Error creando Unattend.xml: $_" "ERROR"
}

# -----------------------------------------------------------------------
# SECCION 9: DESACTIVAR WINDOWS UPDATE (REGISTRO OFFLINE)
# -----------------------------------------------------------------------

Write-Log "Modificando Registro Offline para desactivar Windows Update..." "INFO"

$SoftwareHive = "$TargetDrive\Windows\System32\config\SOFTWARE"
if (Test-Path $SoftwareHive) {
    try {
        reg load HKLM\OfflineSOFTWARE $SoftwareHive 2>&1 | Out-Null
        $AUKey = "HKLM\OfflineSOFTWARE\Policies\Microsoft\Windows\WindowsUpdate\AU"
        
        if (-not (Test-Path "Registry::$AUKey")) {
            New-Item -Path "Registry::$AUKey" -Force | Out-Null
        }
        
        reg add "$AUKey" /v "NoAutoUpdate" /t REG_DWORD /d 1 /f 2>&1 | Out-Null
        reg add "$AUKey" /v "AUOptions" /t REG_DWORD /d 1 /f 2>&1 | Out-Null
        
        [GC]::Collect()
        Start-Sleep -Seconds 2
        reg unload HKLM\OfflineSOFTWARE 2>&1 | Out-Null
        
        Write-Log "Windows Update deshabilitado correctamente" "OK"
    } catch {
        Write-Log "Error deshabilitando Windows Update: $_" "WARN"
    }
} else {
    Write-Log "No se encontro el Hive de registro offline en $SoftwareHive" "WARN"
}

# -----------------------------------------------------------------------
# SECCION 10: COPIAR SETUPCOMPLETE.CMD
# -----------------------------------------------------------------------

Write-Log "Localizando y copiando SetupComplete.cmd..." "INFO"

$ScriptsDest = "$TargetDrive\Windows\Setup\Scripts"
if (-not (Test-Path $ScriptsDest)) {
    New-Item -Path $ScriptsDest -ItemType Directory -Force | Out-Null
}

$SourceScript = "C:\OSDCloud\Scripts\SetupComplete\SetupComplete.cmd"
if (-not (Test-Path $SourceScript)) {
    $SourceScript = $null
    $ScriptDrives = Get-PSDrive -PSProvider FileSystem | Where-Object { $_.Name -ne 'C' }
    foreach ($Drive in $ScriptDrives) {
        foreach ($RelativePath in @(
            "OSDCloud\Config\Scripts\SetupComplete\SetupComplete.cmd",
            "OSDCloud\Scripts\SetupComplete\SetupComplete.cmd"
        )) {
            $Path = Join-Path $Drive.Root $RelativePath
            if (Test-Path $Path) {
                $SourceScript = $Path
                break
            }
        }
        if ($SourceScript) { break }
    }
}

if ($SourceScript) {
    try {
        Copy-Item -Path $SourceScript -Destination "$ScriptsDest\SetupComplete.cmd" -Force
        Write-Log "SetupComplete.cmd copiado desde $SourceScript" "OK"
        
        New-BlueDeployStepJson `
            -DeploymentId $DeploymentId `
            -StepName "SETUPCOMPLETE_COPIED" `
            -StepStatus "SUCCESS" `
            -Message "SetupComplete.cmd copiado correctamente a Scripts" `
            -RawOutput "Source=$SourceScript; Destination=$ScriptsDest" `
            -QueuePath $FinalBlueDeployQueuePath `
            -FileName "030_setupcomplete_copied_step.json" `
            -SourceScript "01-Start-OSDCloud.ps1"
    } catch {
        Write-Log "Error copiando SetupComplete.cmd: $_" "WARN"
        New-BlueDeployStepJson `
            -DeploymentId $DeploymentId `
            -StepName "SETUPCOMPLETE_COPIED" `
            -StepStatus "FAILED" `
            -Message "Error al copiar SetupComplete.cmd" `
            -ErrorCode "COPY_ERROR" `
            -RawOutput "$_" `
            -QueuePath $FinalBlueDeployQueuePath `
            -FileName "030_setupcomplete_copied_step.json" `
            -SourceScript "01-Start-OSDCloud.ps1"
    }
} else {
    Write-Log "No se encontro SetupComplete.cmd en ninguna particion accesible" "WARN"
    New-BlueDeployStepJson `
        -DeploymentId $DeploymentId `
        -StepName "SETUPCOMPLETE_COPIED" `
        -StepStatus "WARNING" `
        -Message "SetupComplete.cmd no encontrado" `
        -ErrorCode "NOT_FOUND" `
        -RawOutput "Buscadas rutas en unidades accesibles" `
        -QueuePath $FinalBlueDeployQueuePath `
        -FileName "030_setupcomplete_copied_step.json" `
        -SourceScript "01-Start-OSDCloud.ps1"
}

# -----------------------------------------------------------------------
# SECCION 11: COPIAR LOG FINAL AL DISCO DURO
# -----------------------------------------------------------------------

Write-Log "Copiando log consolidado al disco duro..." "INFO"

if (Test-Path $LogFile) {
    try {
        Copy-Item -Path $LogFile -Destination "$DestLogPath\" -Force
        Write-Log "Log copiado a $DestLogPath" "OK"
    } catch {
        Write-Log "Error copiando log: $_" "WARN"
    }
}

# -----------------------------------------------------------------------
# SECCION 12: CIERRE DE LA FASE WINPE
# -----------------------------------------------------------------------

Write-Log "Fase WinPE completada; quedan pendientes las tareas SetupComplete y post-instalacion." "OK"

# -----------------------------------------------------------------------
# FINALIZACION
# -----------------------------------------------------------------------

Write-Log "====== FASE WINPE COMPLETADA ======" "OK"
Write-Log "DeploymentId: $DeploymentId" "OK"
Write-Log "Equipo: $NombreEquipo" "OK"
Write-Log "Logs guardados en: $DestLogPath" "OK"
Write-Log "Colas JSON guardadas en: $FinalBlueDeployQueuePath" "OK"

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "   FASE WINPE COMPLETADA EXITOSAMENTE                      " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Write-Host "El equipo se reiniciara para continuar con SetupComplete."
Write-Host "Presione cualquier tecla para reiniciar inmediatamente..."
Write-Host ""
