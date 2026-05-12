# Script to manually clean target directory
# Use this if mvn clean fails due to file locks
# Usage: .\scripts\clean-target.ps1

# Get script directory and project root
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

# Change to project root directory
Push-Location $projectRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Cleaning Target Directory" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$targetDir = "target"

if (-not (Test-Path $targetDir)) {
    Write-Host "[OK] Target directory does not exist, nothing to clean" -ForegroundColor Green
    Pop-Location
    exit 0
}

Write-Host "[INFO] Target directory found: $targetDir" -ForegroundColor Yellow
Write-Host "[INFO] Attempting to remove all files..." -ForegroundColor Yellow
Write-Host ""

# Function to remove directory with retry
function Remove-DirectoryWithRetry {
    param(
        [string]$Path,
        [int]$MaxRetries = 3,
        [int]$DelaySeconds = 2
    )
    
    for ($i = 1; $i -le $MaxRetries; $i++) {
        try {
            if (Test-Path $Path) {
                Write-Host "[INFO] Attempt $i of $MaxRetries..." -ForegroundColor Gray
                
                # Try to unlock files first
                Get-ChildItem -Path $Path -Recurse -Force -ErrorAction SilentlyContinue | 
                    ForEach-Object {
                        try {
                            $_.Attributes = 'Normal'
                        } catch {
                            # Ignore errors
                        }
                    }
                
                Start-Sleep -Seconds 1
                
                # Remove directory
                Remove-Item -Path $Path -Recurse -Force -ErrorAction Stop
                Write-Host "[OK] Successfully removed: $Path" -ForegroundColor Green
                return $true
            } else {
                Write-Host "[OK] Directory already removed" -ForegroundColor Green
                return $true
            }
        } catch {
            if ($i -lt $MaxRetries) {
                Write-Host "[WARNING] Attempt $i failed, waiting ${DelaySeconds}s before retry..." -ForegroundColor Yellow
                Start-Sleep -Seconds $DelaySeconds
            } else {
                Write-Host "[ERROR] Failed to remove after $MaxRetries attempts" -ForegroundColor Red
                Write-Host "[ERROR] Error: $($_.Exception.Message)" -ForegroundColor Red
                return $false
            }
        }
    }
    return $false
}

# Try to remove target directory
$success = Remove-DirectoryWithRetry -Path $targetDir

if ($success) {
    Write-Host ""
    Write-Host "[OK] Target directory cleaned successfully!" -ForegroundColor Green
    Write-Host "[INFO] You can now run: .\scripts\build.ps1" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "[ERROR] Could not clean target directory" -ForegroundColor Red
    Write-Host "[TIP] Try the following:" -ForegroundColor Yellow
    Write-Host "  1. Close your IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)" -ForegroundColor White
    Write-Host "  2. Stop any running Java processes:" -ForegroundColor White
    Write-Host "     Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force" -ForegroundColor Gray
    Write-Host "  3. Close any file explorers showing the target directory" -ForegroundColor White
    Write-Host "  4. Run this script again" -ForegroundColor White
    Write-Host ""
    Write-Host "[NOTE] You can still build without cleaning:" -ForegroundColor Cyan
    Write-Host "  mvn package -DskipTests" -ForegroundColor White
    Pop-Location
    exit 1
}

Pop-Location
Write-Host ""

