<#
.SYNOPSIS
Envía una cola de eventos JSON locales a la API BlueDeploy aplicando el contrato estricto del payload.
#>

param(
    [Parameter(Mandatory = $true)]
    [string]$QueuePath,

    [string]$ApiUrl = $(if ($env:BLUEDEPLOY_API_URL) { $env:BLUEDEPLOY_API_URL } else { "http://178.105.231.10" }),

    [string]$ApiKey = $(if ($env:BLUEDEPLOY_API_KEY) { $env:BLUEDEPLOY_API_KEY } else { "bluedeploy-secret-key-change-in-prod" }),

    [switch]$ContinueOnError
)

if (-not (Test-Path $QueuePath)) {
    Write-Host "[BlueDeploy] Ruta de cola no encontrada: $QueuePath" -ForegroundColor Red
    exit 1
}

# Obtener archivos JSON ordenados alfabéticamente de forma estricta (030, 040, 050...)
$files = Get-ChildItem -Path $QueuePath -Filter "*.json" | Sort-Object Name

if ($files.Count -eq 0) {
    Write-Host "[BlueDeploy] No se encontraron archivos JSON en la cola: $QueuePath" -ForegroundColor Yellow
    exit 0
}

Write-Host "[BlueDeploy] Procesando cola: $QueuePath" -ForegroundColor Cyan
Write-Host "[BlueDeploy] API URL: $ApiUrl" -ForegroundColor Cyan
Write-Host "[BlueDeploy] Archivos detectados: $($files.Count)" -ForegroundColor Cyan
Write-Host ""

foreach ($file in $files) {
    try {
        Write-Host "[BlueDeploy] Leyendo $($file.Name)..." -ForegroundColor Gray

        # Leer contenido crudo del archivo
        $rawJson = Get-Content $file.FullName -Raw
        $event   = $rawJson | ConvertFrom-Json

        # Validaciones de estructura interna del archivo en cola
        if (-not $event.apiPath) {
            throw "Falta el campo 'apiPath' en el archivo $($file.Name)"
        }
        if (-not $event.payload) {
            throw "Falta el objeto 'payload' en el archivo $($file.Name)"
        }

        # Construir la URL destino combinando el host y el apiPath del JSON
        $uri = "$ApiUrl$($event.apiPath)"

        # CORRECCIÓN DE CONTRATO REST: Enviamos única y estrictamente el bloque 'payload'
        # re-convertido a JSON con profundidad máxima para evitar truncamientos en sub-objetos.
        $body = $event.payload | ConvertTo-Json -Depth 10

        Write-Host "  -> POST $uri" -ForegroundColor DarkCyan

        # Construir cabeceras HTTP (incluyendo X-BlueDeploy-Api-Key si está disponible)
        $headers = @{}
        if ($ApiKey) {
            $headers["X-BlueDeploy-Api-Key"] = $ApiKey
        }

        # Realizar invocación HTTP hacia el backend
        $response = Invoke-RestMethod `
            -Method POST `
            -Uri $uri `
            -ContentType "application/json" `
            -Headers $headers `
            -Body $body

        Write-Host "  [OK] Enviado con éxito $($event.eventType) para deploymentId=$($event.deploymentId)" -ForegroundColor Green

        # ELIMINACIÓN SEGURA: Se borra localmente solo si la API respondió con éxito (HTTP 200/201)
        Remove-Item -Path $file.FullName -Force | Out-Null
        Write-Host "  [OK] Archivo $($file.Name) purgado de la cola local." -ForegroundColor DarkGreen
    }
    catch {
        # Captura extendida de excepciones HTTP para obtener el detalle interno si es posible
        $ErrorMsg = $_.Exception.Message
        if ($_.Exception.Response) {
            $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $ServerDetails = $Reader.ReadToEnd()
            $ErrorMsg += " | Detalles del Servidor: $ServerDetails"
        }

        Write-Host "  [ERROR] Fallo al procesar $($file.Name): $ErrorMsg" -ForegroundColor Red

        if (-not $ContinueOnError) {
            Write-Host "[BlueDeploy] Procesamiento de cola interrumpido debido a un error crítico." -ForegroundColor Red
            exit 1
        }
    }
}

Write-Host ""
Write-Host "[BlueDeploy] Sincronización de cola finalizada." -ForegroundColor Green