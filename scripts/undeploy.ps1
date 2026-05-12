# Script to undeploy/stop LiteFlow from Tomcat
# Usage: .\scripts\undeploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0"

param(
    [string]$TomcatHome = "",
    [switch]$StopTomcat = $false,
    [switch]$StartTomcat = $false
)

# Get script directory and project root
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Undeploy LiteFlow from Tomcat" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Find Tomcat home if not specified
if ([string]::IsNullOrEmpty($TomcatHome)) {
    $possiblePaths = @(
        "$env:ProgramFiles\Apache Software Foundation\Tomcat 11.0",
        "$env:ProgramFiles\Apache Software Foundation\Tomcat 10.1",
        "$env:ProgramFiles\Apache Software Foundation\Tomcat 10.0",
        "$env:ProgramFiles\Apache Software Foundation\Tomcat 9.0",
        "C:\apache-tomcat-11.0.0",
        "C:\apache-tomcat-10.1.0",
        "C:\apache-tomcat-10.0.0",
        "C:\apache-tomcat-9.0.0",
        "C:\tomcat",
        "$env:CATALINA_HOME"
    )
    
    foreach ($path in $possiblePaths) {
        if (Test-Path $path) {
            $webappsPath = Join-Path $path "webapps"
            if (Test-Path $webappsPath) {
                $TomcatHome = $path
                break
            }
        }
    }
    
    if ([string]::IsNullOrEmpty($TomcatHome)) {
        Write-Host "[ERROR] Tomcat installation not found!" -ForegroundColor Red
        Write-Host "Please specify TomcatHome:" -ForegroundColor Yellow
        Write-Host "  .\scripts\undeploy.ps1 -TomcatHome `"C:\Program Files\Apache Software Foundation\Tomcat 11.0`"" -ForegroundColor White
        exit 1
    }
}

Write-Host "[OK] Tomcat Home: $TomcatHome" -ForegroundColor Green

# Check required directories
$webappsPath = Join-Path $TomcatHome "webapps"
$binPath = Join-Path $TomcatHome "bin"

if (-not (Test-Path $webappsPath)) {
    Write-Host "[ERROR] webapps directory not found: $webappsPath" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $binPath)) {
    Write-Host "[ERROR] bin directory not found: $binPath" -ForegroundColor Red
    exit 1
}

# Stop Tomcat if requested or if we need to remove files
if ($StopTomcat) {
    Write-Host ""
    Write-Host "[INFO] Stopping Tomcat..." -ForegroundColor Yellow
    $stopScript = Join-Path $binPath "shutdown.bat"
    if (Test-Path $stopScript) {
        & $stopScript
        Start-Sleep -Seconds 5
        Write-Host "[OK] Tomcat stopped" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] shutdown.bat not found, please stop Tomcat manually" -ForegroundColor Yellow
        Write-Host "[WARNING] You may need to stop Tomcat manually before undeploying" -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "[WARNING] Tomcat may still be running" -ForegroundColor Yellow
    Write-Host "[INFO] If files are locked, stop Tomcat first:" -ForegroundColor Cyan
    Write-Host "  .\scripts\undeploy.ps1 -StopTomcat" -ForegroundColor White
    Write-Host ""
}

# Remove deployed application
$oldAppPath = Join-Path $webappsPath "LiteFlow"
$oldWarPath = Join-Path $webappsPath "LiteFlow.war"

Write-Host ""
Write-Host "[INFO] Removing deployed application..." -ForegroundColor Yellow

$removed = $false

# Remove extracted application directory
if (Test-Path $oldAppPath) {
    try {
        Remove-Item -Path $oldAppPath -Recurse -Force -ErrorAction Stop
        Write-Host "   [OK] Removed: $oldAppPath" -ForegroundColor Green
        $removed = $true
    } catch {
        Write-Host "   [ERROR] Could not remove: $oldAppPath" -ForegroundColor Red
        Write-Host "   [ERROR] Error: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "   [TIP] Try stopping Tomcat first: .\scripts\undeploy.ps1 -StopTomcat" -ForegroundColor Yellow
    }
} else {
    Write-Host "   [INFO] Application directory not found: $oldAppPath" -ForegroundColor Gray
}

# Remove WAR file
if (Test-Path $oldWarPath) {
    try {
        Remove-Item -Path $oldWarPath -Force -ErrorAction Stop
        Write-Host "   [OK] Removed: $oldWarPath" -ForegroundColor Green
        $removed = $true
    } catch {
        Write-Host "   [ERROR] Could not remove: $oldWarPath" -ForegroundColor Red
        Write-Host "   [ERROR] Error: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "   [TIP] Try stopping Tomcat first: .\scripts\undeploy.ps1 -StopTomcat" -ForegroundColor Yellow
    }
} else {
    Write-Host "   [INFO] WAR file not found: $oldWarPath" -ForegroundColor Gray
}

if (-not $removed) {
    Write-Host ""
    Write-Host "[WARNING] No deployment found or could not be removed" -ForegroundColor Yellow
    Write-Host "[INFO] Application may not be deployed, or files are locked" -ForegroundColor Cyan
}

# Start Tomcat if requested
if ($StartTomcat) {
    Write-Host ""
    Write-Host "[INFO] Starting Tomcat..." -ForegroundColor Yellow
    $startScript = Join-Path $binPath "startup.bat"
    if (Test-Path $startScript) {
        Start-Process -FilePath $startScript -WindowStyle Normal
        Write-Host "[OK] Tomcat started" -ForegroundColor Green
        Write-Host ""
        Write-Host "[INFO] Waiting 5 seconds for Tomcat to start..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    } else {
        Write-Host "[WARNING] startup.bat not found, please start Tomcat manually" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($removed) {
    Write-Host "[OK] Undeployment completed!" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Undeployment completed with warnings" -ForegroundColor Yellow
}
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "[INFO] LiteFlow has been removed from Tomcat" -ForegroundColor Cyan
Write-Host ""
Write-Host "[NOTE] If files were locked, run with -StopTomcat flag:" -ForegroundColor Yellow
Write-Host "  .\scripts\undeploy.ps1 -StopTomcat" -ForegroundColor White
Write-Host ""

