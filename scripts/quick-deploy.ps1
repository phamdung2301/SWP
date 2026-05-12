# Quick script: Build and Deploy in one step
# Usage: .\scripts\quick-deploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0"

param(
    [string]$TomcatHome = ""
)

# Get script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Quick Deploy LiteFlow" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Build
Write-Host "[STEP 1] Building WAR file..." -ForegroundColor Yellow
& (Join-Path $scriptDir "build.ps1")

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[STEP 2] Deploying to Tomcat..." -ForegroundColor Yellow

# Step 2: Deploy
if ([string]::IsNullOrEmpty($TomcatHome)) {
    & (Join-Path $scriptDir "deploy.ps1") -StopTomcat -StartTomcat
} else {
    & (Join-Path $scriptDir "deploy.ps1") -TomcatHome $TomcatHome -StopTomcat -StartTomcat
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Deploy failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[OK] Complete! Application deployed at:" -ForegroundColor Green
Write-Host "   http://localhost:8080/LiteFlow" -ForegroundColor Cyan
Write-Host ""

