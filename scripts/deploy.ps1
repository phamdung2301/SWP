# Script to deploy LiteFlow to Tomcat
# Usage: .\scripts\deploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0"

param(
    [string]$TomcatHome = "",
    [string]$WarFile = "",
    [switch]$StopTomcat = $false,
    [switch]$StartTomcat = $false
)

# Get script directory and project root
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

# Set default WAR file path relative to project root
if ([string]::IsNullOrEmpty($WarFile)) {
    $WarFile = Join-Path $projectRoot "target\LiteFlow.war"
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Deploy LiteFlow to Tomcat" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check WAR file
if (-not (Test-Path $WarFile)) {
    Write-Host "[ERROR] WAR file not found: $WarFile" -ForegroundColor Red
    Write-Host "Please run .\scripts\build.ps1 first to build the WAR file" -ForegroundColor Yellow
    exit 1
}

Write-Host "[OK] WAR file found: $WarFile" -ForegroundColor Green
$fileSize = (Get-Item $WarFile).Length / 1MB
Write-Host "[INFO] Size: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Cyan
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
        Write-Host "  .\scripts\deploy.ps1 -TomcatHome `"C:\Program Files\Apache Software Foundation\Tomcat 11.0`"" -ForegroundColor White
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

# Stop Tomcat if requested
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
    }
}

# Remove old deployment if exists
$oldAppPath = Join-Path $webappsPath "LiteFlow"
$oldWarPath = Join-Path $webappsPath "LiteFlow.war"

Write-Host ""
Write-Host "[INFO] Removing old deployment..." -ForegroundColor Yellow

if (Test-Path $oldAppPath) {
    Remove-Item -Path $oldAppPath -Recurse -Force
    Write-Host "   [OK] Removed: $oldAppPath" -ForegroundColor Gray
}

if (Test-Path $oldWarPath) {
    Remove-Item -Path $oldWarPath -Force
    Write-Host "   [OK] Removed: $oldWarPath" -ForegroundColor Gray
}

# Copy new WAR file
Write-Host ""
Write-Host "[INFO] Deploying new WAR file..." -ForegroundColor Yellow
Copy-Item -Path $WarFile -Destination $oldWarPath -Force
Write-Host "[OK] Deployed: $oldWarPath" -ForegroundColor Green

# Check context.xml
$contextXml = Join-Path $webappsPath "LiteFlow\META-INF\context.xml"
if (Test-Path $contextXml) {
    Write-Host ""
    Write-Host "[OK] Context.xml found" -ForegroundColor Green
    Write-Host "   Location: $contextXml" -ForegroundColor Gray
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
        Write-Host "[INFO] Waiting 10 seconds for Tomcat to start..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
    } else {
        Write-Host "[WARNING] startup.bat not found, please start Tomcat manually" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[OK] Deployment completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "[INFO] Application will be available at:" -ForegroundColor Yellow
Write-Host "   http://localhost:8080/LiteFlow" -ForegroundColor White
Write-Host ""
Write-Host "[NOTE] Important:" -ForegroundColor Yellow
Write-Host "   - Make sure Tomcat is running" -ForegroundColor White
Write-Host "   - Check database connection in context.xml" -ForegroundColor White
Write-Host "   - View logs at: $TomcatHome\logs" -ForegroundColor White
Write-Host ""

