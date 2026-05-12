# Script to build WAR file for LiteFlow
# Usage: .\scripts\build.ps1
# Or from project root: .\scripts\build.ps1

# Get script directory and project root
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

# Change to project root directory
Push-Location $projectRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Building LiteFlow WAR File" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Maven is installed
$mavenCmd = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mavenCmd) {
    Write-Host "[ERROR] Maven not found!" -ForegroundColor Red
    Write-Host "Please install Maven and add it to PATH" -ForegroundColor Yellow
    Pop-Location
    exit 1
}

Write-Host "[OK] Maven found: $($mavenCmd.Source)" -ForegroundColor Green
Write-Host ""

# Clean and build project
Write-Host "[INFO] Cleaning project..." -ForegroundColor Yellow

# Try to manually clean target directory first to avoid file lock issues
$targetDir = "target"
if (Test-Path $targetDir) {
    Write-Host "[INFO] Attempting to remove target directory..." -ForegroundColor Gray
    try {
        # Try to remove files that might be locked
        Get-ChildItem -Path $targetDir -Recurse -Force -ErrorAction SilentlyContinue | 
            Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
        Start-Sleep -Milliseconds 500
    } catch {
        Write-Host "[WARNING] Some files could not be removed, continuing anyway..." -ForegroundColor Yellow
    }
}

# Run Maven clean (suppress output to avoid clutter)
$cleanOutput = mvn clean 2>&1
$cleanSuccess = $LASTEXITCODE -eq 0

if (-not $cleanSuccess) {
    Write-Host "[WARNING] Maven clean had some issues (files may be locked by IDE/process)" -ForegroundColor Yellow
    Write-Host "[INFO] Continuing with build anyway (package will rebuild)..." -ForegroundColor Cyan
    Write-Host "[TIP] If you see this warning, try:" -ForegroundColor Gray
    Write-Host "     - Close your IDE (IntelliJ, Eclipse, etc.)" -ForegroundColor Gray
    Write-Host "     - Stop any running test processes" -ForegroundColor Gray
    Write-Host "     - Run: .\scripts\clean-target.ps1 to manually clean" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host "[OK] Clean successful" -ForegroundColor Green
}

Write-Host ""
Write-Host "[INFO] Building WAR file..." -ForegroundColor Yellow
mvn package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Build failed!" -ForegroundColor Red
    Pop-Location
    exit 1
}

# Check if WAR file was created
$warFile = "target\LiteFlow.war"
if (Test-Path $warFile) {
    $fileSize = (Get-Item $warFile).Length / 1MB
    Write-Host ""
    Write-Host "[OK] Build successful!" -ForegroundColor Green
    Write-Host "[INFO] WAR file: $warFile" -ForegroundColor Cyan
    Write-Host "[INFO] Size: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "[INFO] You can deploy this WAR file to Tomcat:" -ForegroundColor Yellow
    Write-Host "  - Copy to: `$TOMCAT_HOME\webapps\LiteFlow.war" -ForegroundColor White
    Write-Host "  - Or use: .\scripts\deploy.ps1" -ForegroundColor White
} else {
    Write-Host "[ERROR] WAR file was not created!" -ForegroundColor Red
    Pop-Location
    exit 1
}

Pop-Location

