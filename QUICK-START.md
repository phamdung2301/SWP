# Quick Start Guide - Deploy LiteFlow to Tomcat

## Prerequisites
- Java JDK 16+
- Apache Tomcat 11.0 (or 10.1+)
- Maven 3.6+
- SQL Server running
- Database `LiteFlowDBO` created

## Quick Deploy (Recommended)

### Option 1: Auto-detect Tomcat
```powershell
.\scripts\quick-deploy.ps1
```

### Option 2: Specify Tomcat Path
```powershell
.\scripts\quick-deploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0"
```

## Step-by-Step Deploy
sss
### Step 1: Build WAR File
```powershell
.\scripts\build.ps1
```

### Step 2: Deploy to Tomcat
```powershell
# Auto-detect Tomcat
.\scripts\deploy.ps1 -StopTomcat -StartTomcat

# Or specify Tomcat path
.\scripts\deploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0" -StopTomcat -StartTomcat
```

## Access Application
After deployment, access:
- **Home**: http://localhost:8080/LiteFlow
- **Login**: http://localhost:8080/LiteFlow/auth/login.jsp

## Undeploy Application

### Remove Deployment
```powershell
# Stop Tomcat and remove deployment
.\scripts\undeploy.ps1 -StopTomcat

# Or specify Tomcat path
.\scripts\undeploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0" -StopTomcat
```

This will:
- Stop Tomcat (if `-StopTomcat` is used)
- Remove WAR file from webapps
- Remove extracted application directory
- Start Tomcat again (if `-StartTomcat` is used)

## Troubleshooting

### If Tomcat not found:
```powershell
.\scripts\deploy.ps1 -TomcatHome "C:\Program Files\Apache Software Foundation\Tomcat 11.0"
```

### If execution policy error:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Check logs:
```
C:\Program Files\Apache Software Foundation\Tomcat 11.0\logs\catalina.out
```

## Notes
- All scripts now output in English to avoid font issues
- Tomcat 11.0 is automatically detected
- Scripts will stop/start Tomcat automatically if flags are used

