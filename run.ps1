param(
    [ValidateSet("start", "stop")]
    [string]$Action = "start"
)

$ErrorActionPreference = "Stop"
$projectDir = $PSScriptRoot
$baseDir = Join-Path $projectDir ".runtime\tomcat-base"

$tomcatCandidates = @(@(
    $env:CATALINA_HOME,
    "C:\Program Files\Apache Software Foundation\Tomcat 10.1",
    "C:\Program Files\Apache Software Foundation\Tomcat 10.1_Tomcat10.1"
) | Where-Object { $_ -and (Test-Path (Join-Path $_ "bin\catalina.bat")) })

if (-not $tomcatCandidates) {
    throw "Khong tim thay Tomcat 10.1. Hay cai Tomcat hoac dat bien CATALINA_HOME."
}

$tomcatHome = $tomcatCandidates[0]
$env:CATALINA_HOME = $tomcatHome
$env:CATALINA_BASE = $baseDir

$javaCandidates = @(@(
    $env:JAVA_HOME,
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Java\latest"
) | Where-Object { $_ -and (Test-Path (Join-Path $_ "bin\java.exe")) })

if (-not $javaCandidates) {
    throw "Khong tim thay JDK 17. Hay cai JDK 17 hoac dat bien JAVA_HOME."
}

$env:JAVA_HOME = $javaCandidates[0]

if ($Action -eq "stop") {
    if (Test-Path $baseDir) {
        & (Join-Path $tomcatHome "bin\catalina.bat") stop
    } else {
        Write-Host "Tomcat chua duoc khoi tao cho du an nay."
    }
    exit $LASTEXITCODE
}

Write-Host "Building project..." -ForegroundColor Cyan
& (Join-Path $projectDir "mvnw.cmd") clean package
if ($LASTEXITCODE -ne 0) {
    throw "Build that bai."
}

foreach ($folder in @("conf", "logs", "temp", "webapps", "work")) {
    New-Item -ItemType Directory -Path (Join-Path $baseDir $folder) -Force | Out-Null
}

if (-not (Test-Path (Join-Path $baseDir "conf\server.xml"))) {
    Copy-Item (Join-Path $tomcatHome "conf\*") (Join-Path $baseDir "conf") -Recurse -Force
}

# The Windows installer disables Tomcat's shutdown port by default, which makes
# a project-local instance impossible to stop cleanly. Enable it only in CATALINA_BASE.
$serverXml = Join-Path $baseDir "conf\server.xml"
$serverConfig = Get-Content $serverXml -Raw
if ($serverConfig -match '<Server port="-1"') {
    $serverConfig.Replace('<Server port="-1"', '<Server port="8005"') |
        Set-Content $serverXml -Encoding UTF8
}

# A normal `run.ps1` is also a safe restart. This avoids launching a second
# Tomcat while an older project instance still owns port 8080.
$existingListener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($existingListener) {
    Write-Host "Stopping previous Tomcat instance..." -ForegroundColor Yellow
    & (Join-Path $tomcatHome "bin\catalina.bat") stop 2>$null
    for ($attempt = 0; $attempt -lt 15; $attempt++) {
        Start-Sleep -Seconds 1
        if (-not (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue)) {
            break
        }
    }
    if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
        throw "Port 8080 dang bi mot tien trinh khac su dung. Hay dong tien trinh do roi chay lai."
    }
}

$explodedApp = Join-Path $baseDir "webapps\hrms-admin-demo"
if (Test-Path $explodedApp) {
    $resolvedApp = (Resolve-Path $explodedApp).Path
    $resolvedWebapps = (Resolve-Path (Join-Path $baseDir "webapps")).Path
    if (-not $resolvedApp.StartsWith($resolvedWebapps + [IO.Path]::DirectorySeparatorChar)) {
        throw "Duong dan deploy khong an toan: $resolvedApp"
    }
    Remove-Item -LiteralPath $resolvedApp -Recurse -Force
}

Copy-Item (Join-Path $projectDir "target\hrms-admin-demo.war") `
    (Join-Path $baseDir "webapps\hrms-admin-demo.war") -Force

Write-Host "Starting Tomcat..." -ForegroundColor Cyan
& (Join-Path $tomcatHome "bin\catalina.bat") start
if ($LASTEXITCODE -ne 0) {
    throw "Khong the khoi dong Tomcat."
}

$url = "http://localhost:8080/hrms-admin-demo/"
$healthUrl = "http://localhost:8080/hrms-admin-demo/login"
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    try {
        Invoke-WebRequest $healthUrl -UseBasicParsing -TimeoutSec 2 | Out-Null
        Write-Host "App is running: $url" -ForegroundColor Green
        Start-Process $url
        exit 0
    } catch {
        Start-Sleep -Seconds 1
    }
}

throw "Tomcat da khoi dong nhung app chua phan hoi. Kiem tra .runtime\tomcat-base\logs."
