# Deployment Scripts

This folder contains all deployment and build scripts for LiteFlow.

## Scripts Overview

| Script | Description |
|--------|-------------|
| `build.ps1` | Build WAR file from source code |
| `deploy.ps1` | Deploy WAR file to Tomcat |
| `undeploy.ps1` | Remove/stop deployment from Tomcat |
| `quick-deploy.ps1` | Build and deploy in one step |
| `clean-target.ps1` | Manually clean target directory (if mvn clean fails) |

## Quick Start

### Option 1: Quick Deploy (Recommended)
```powershell
.\scripts\quick-deploy.ps1
```

### Option 2: Step by Step
```powershell
# 1. Build WAR file
.\scripts\build.ps1

# 2. Deploy to Tomcat
.\scripts\deploy.ps1 -StopTomcat -StartTomcat
```

## Usage

### Build Script
```powershell
.\scripts\build.ps1
```
- Cleans project
- Builds WAR file
- Output: `target\LiteFlow.war`

### Deploy Script
```powershell
# Auto-detect Tomcat
.\scripts\deploy.ps1 -StopTomcat -StartTomcat

# Specify Tomcat path
.\scripts\deploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0" -StopTomcat -StartTomcat
```

**Parameters:**
- `-TomcatHome`: Path to Tomcat installation (optional, auto-detected)
- `-WarFile`: Path to WAR file (optional, defaults to `target\LiteFlow.war`)
- `-StopTomcat`: Stop Tomcat before deployment
- `-StartTomcat`: Start Tomcat after deployment

### Quick Deploy Script
```powershell
.\scripts\quick-deploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0"
```
- Builds WAR file
- Deploys to Tomcat
- Stops and starts Tomcat automatically

### Undeploy Script
```powershell
# Remove deployment (stop Tomcat first if files are locked)
.\scripts\undeploy.ps1 -StopTomcat

# Remove deployment and restart Tomcat
.\scripts\undeploy.ps1 -StopTomcat -StartTomcat

# Specify Tomcat path
.\scripts\undeploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0" -StopTomcat
```
- Removes deployed WAR file and application directory
- Stops Tomcat if requested
- Use `-StopTomcat` if files are locked

**Parameters:**
- `-TomcatHome`: Path to Tomcat installation (optional, auto-detected)
- `-StopTomcat`: Stop Tomcat before removing files
- `-StartTomcat`: Start Tomcat after removing files

### Clean Target Script
```powershell
.\scripts\clean-target.ps1
```
- Manually removes target directory
- Use when `mvn clean` fails due to file locks

## Notes

- All scripts work from project root directory
- Scripts automatically detect project root relative to script location
- All paths are relative to project root
- Scripts output in English to avoid font issues

## Troubleshooting

### File Lock Errors
If you get file lock errors during build:
1. Close your IDE (IntelliJ, Eclipse, VS Code, etc.)
2. Stop Java processes: `Get-Process java | Stop-Process -Force`
3. Run: `.\scripts\clean-target.ps1`
4. Try build again: `.\scripts\build.ps1`

### Tomcat Not Found
Specify Tomcat path explicitly:
```powershell
.\scripts\deploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0"
```

### Execution Policy Error
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

