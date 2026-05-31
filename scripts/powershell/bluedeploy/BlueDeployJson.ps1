<#
.SYNOPSIS
Funciones auxiliares para generar eventos JSON de BlueDeploy.

.DESCRIPTION
Este script no envía datos a la API. Solo genera archivos JSON locales
que posteriormente pueden enviarse con Send-BlueDeployQueue.ps1.

La cola local recomendada para equipos desplegados es:

C:\tmp\BlueDeploy\Queue
#>

function New-BlueDeployQueueDirectory {
    param(
        [string]$QueuePath = "C:\tmp\BlueDeploy\Queue"
    )

    if (-not (Test-Path $QueuePath)) {
        New-Item -Path $QueuePath -ItemType Directory -Force | Out-Null
    }

    return $QueuePath
}

function New-BlueDeployDeploymentId {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Hostname
    )

    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    return "DEPLOY-$Hostname-$timestamp"
}

function Save-BlueDeployEvent {
    param(
        [Parameter(Mandatory = $true)]
        [string]$QueuePath,

        [Parameter(Mandatory = $true)]
        [string]$FileName,

        [Parameter(Mandatory = $true)]
        [object]$Event
    )

    New-BlueDeployQueueDirectory -QueuePath $QueuePath | Out-Null

    $path = Join-Path $QueuePath $FileName

    $Event |
        ConvertTo-Json -Depth 10 |
        Out-File -FilePath $path -Encoding UTF8 -Force

    Write-Host "[BlueDeploy] JSON generated: $path" -ForegroundColor Cyan

    return $path
}

function New-BlueDeployDeploymentJson {
    param(
        [Parameter(Mandatory = $true)]
        [string]$DeploymentId,

        [Parameter(Mandatory = $true)]
        [string]$Hostname,

        [string]$DeviceModel = "UNKNOWN",

        [string]$DomainName = "corp.local",

        [string]$QueuePath = "C:\tmp\BlueDeploy\Queue",

        [string]$FileName = "000_deployment.json",

        [string]$SourceScript = "UNKNOWN"
    )

    $event = [ordered]@{
        eventType    = "DEPLOYMENT"
        deploymentId = $DeploymentId
        apiPath      = "/api/deployments"
        createdAt    = (Get-Date).ToString("s")
        sourceScript = $SourceScript
        payload      = [ordered]@{
            deploymentId = $DeploymentId
            hostname     = $Hostname
            deviceModel  = $DeviceModel
            domainName   = $DomainName
        }
    }

    Save-BlueDeployEvent -QueuePath $QueuePath -FileName $FileName -Event $event
}

function New-BlueDeployStepJson {
    param(
        [Parameter(Mandatory = $true)]
        [string]$DeploymentId,

        [Parameter(Mandatory = $true)]
        [string]$StepName,

        [Parameter(Mandatory = $true)]
        [ValidateSet("PENDING", "RUNNING", "SUCCESS", "WARNING", "FAILED", "SKIPPED")]
        [string]$StepStatus,

        [string]$Message = "",

        [string]$ErrorCode = $null,

        [string]$RawOutput = "",

        [string]$QueuePath = "C:\tmp\BlueDeploy\Queue",

        [Parameter(Mandatory = $true)]
        [string]$FileName,

        [string]$SourceScript = "UNKNOWN"
    )

    $event = [ordered]@{
        eventType    = "DEPLOYMENT_STEP"
        deploymentId = $DeploymentId
        apiPath      = "/api/deployments/$DeploymentId/steps"
        createdAt    = (Get-Date).ToString("s")
        sourceScript = $SourceScript
        payload      = [ordered]@{
            stepName   = $StepName
            stepStatus = $StepStatus
            message    = $Message
            errorCode  = $ErrorCode
            rawOutput  = $RawOutput
        }
    }

    Save-BlueDeployEvent -QueuePath $QueuePath -FileName $FileName -Event $event
}

function New-BlueDeploySecurityCheckJson {
    param(
        [Parameter(Mandatory = $true)]
        [string]$DeploymentId,

        [Parameter(Mandatory = $true)]
        [string]$CheckName,

        [Parameter(Mandatory = $true)]
        [ValidateSet("PASSED", "FAILED", "WARNING", "PENDING", "UNKNOWN")]
        [string]$CheckStatus,

        [Parameter(Mandatory = $true)]
        [ValidateSet("LOW", "MEDIUM", "HIGH", "CRITICAL")]
        [string]$Severity,

        [string]$ExpectedValue = "",

        [string]$ActualValue = "",

        [string]$Message = "",

        [string]$QueuePath = "C:\tmp\BlueDeploy\Queue",

        [Parameter(Mandatory = $true)]
        [string]$FileName,

        [string]$SourceScript = "UNKNOWN"
    )

    $event = [ordered]@{
        eventType    = "SECURITY_CHECK"
        deploymentId = $DeploymentId
        apiPath      = "/api/deployments/$DeploymentId/checks"
        createdAt    = (Get-Date).ToString("s")
        sourceScript = $SourceScript
        payload      = [ordered]@{
            checkName     = $CheckName
            checkStatus   = $CheckStatus
            severity      = $Severity
            expectedValue = $ExpectedValue
            actualValue   = $ActualValue
            message       = $Message
        }
    }

    Save-BlueDeployEvent -QueuePath $QueuePath -FileName $FileName -Event $event
}