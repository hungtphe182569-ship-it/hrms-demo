param(
    [ValidateSet("start", "stop")]
    [string]$Action = "start"
)

$ErrorActionPreference = "Stop"
$projectDir = $PSScriptRoot
$baseDir = Join-Path $projectDir ".runtime\tomcat-base"
$runtimeDir = Join-Path $projectDir ".runtime"

function Find-JavaHome {
    $candidates = New-Object System.Collections.Generic.List[string]
    if ($env:JAVA_HOME) { [void]$candidates.Add($env:JAVA_HOME.Trim()) }

    $searchRoots = @(
        "C:\Program Files\Microsoft\jdk-17*",
        "C:\Program Files\Eclipse Adoptium\jdk-17*",
        "C:\Program Files\Java\jdk-17*",
        "C:\Program Files\Java\latest"
    )
    foreach ($pattern in $searchRoots) {
        Get-Item $pattern -ErrorAction SilentlyContinue | ForEach-Object {
            [void]$candidates.Add($_.FullName)
        }
    }

    foreach ($homePath in $candidates) {
        if ($homePath -and (Test-Path (Join-Path $homePath "bin\java.exe"))) {
            return $homePath
        }
    }
    return $null
}

function Find-TomcatHome {
    $candidates = New-Object System.Collections.Generic.List[string]
    if ($env:CATALINA_HOME) { [void]$candidates.Add($env:CATALINA_HOME.Trim()) }
    [void]$candidates.Add((Join-Path $runtimeDir "tomcat-10.1"))
    [void]$candidates.Add("C:\Program Files\Apache Software Foundation\Tomcat 10.1")
    [void]$candidates.Add("C:\Program Files\Apache Software Foundation\Tomcat 10.1_Tomcat10.1")

    Get-ChildItem (Join-Path $runtimeDir "apache-tomcat-10.1*") -Directory -ErrorAction SilentlyContinue | ForEach-Object {
        [void]$candidates.Add($_.FullName)
    }

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path (Join-Path $candidate "bin\catalina.bat"))) {
            return (Resolve-Path $candidate).Path
        }
    }
    return $null
}

function Get-ConfiguredDbEndpoint {
    $url = $env:HRMS_DB_URL
    if (-not $url) {
        $url = "jdbc:mysql://localhost:3306/HRMS_Demo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh&useSSL=false&allowPublicKeyRetrieval=true"
    }

    if ($url -notmatch '^jdbc:mysql://([^/?]+)') {
        return $null
    }

    $serverPart = $Matches[1].Trim()
    if (-not $serverPart -or $serverPart.Contains("\")) {
        return $null
    }

    $serverHost = $serverPart
    $serverPort = 3306
    if ($serverPart -match '^\[([^\]]+)\]:(\d+)$') {
        $serverHost = $Matches[1]
        $serverPort = [int]$Matches[2]
    } elseif ($serverPart -match '^([^:]+):(\d+)$') {
        $serverHost = $Matches[1]
        $serverPort = [int]$Matches[2]
    }

    [pscustomobject]@{
        Host = $serverHost
        Port = $serverPort
    }
}

function Test-TcpPort {
    param(
        [string]$HostName,
        [int]$Port,
        [int]$TimeoutMs = 1500
    )

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $asyncResult = $client.BeginConnect($HostName, $Port, $null, $null)
        if (-not $asyncResult.AsyncWaitHandle.WaitOne($TimeoutMs, $false)) {
            return $false
        }
        $client.EndConnect($asyncResult)
        return $true
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

function Clear-StaleTomcatContexts {
    param([string]$BaseDirectory)

    $contextDir = Join-Path $BaseDirectory "conf\Catalina\localhost"
    if (-not (Test-Path $contextDir)) {
        return
    }

    $resolvedContextDir = (Resolve-Path $contextDir).Path
    $resolvedBaseDir = (Resolve-Path $BaseDirectory).Path
    $basePrefix = $resolvedBaseDir + [IO.Path]::DirectorySeparatorChar
    if (-not $resolvedContextDir.StartsWith($basePrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Duong dan context Tomcat khong an toan: $resolvedContextDir"
    }

    $contextFiles = @(Get-ChildItem -LiteralPath $resolvedContextDir -Filter "*.xml" -File -ErrorAction SilentlyContinue)
    if ($contextFiles.Count -gt 0) {
        Write-Host "Removing stale Tomcat context descriptors from project runtime..." -ForegroundColor Yellow
        $contextFiles | Remove-Item -Force
    }
}

function Ensure-Tomcat {
    $existing = Find-TomcatHome
    if ($existing) { return $existing }

    New-Item -ItemType Directory -Path $runtimeDir -Force | Out-Null
    $zipPath = Join-Path $runtimeDir "tomcat-10.1.zip"
    $extractRoot = Join-Path $runtimeDir "tomcat-extract"
    $targetHome = Join-Path $runtimeDir "tomcat-10.1"

    # Prefer a stable 10.1.x zip from Apache archive.
    $downloadUrl = "https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.34/bin/apache-tomcat-10.1.34-windows-x64.zip"
    Write-Host "Downloading portable Tomcat 10.1 into .runtime ..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath -UseBasicParsing

    if (Test-Path $extractRoot) { Remove-Item $extractRoot -Recurse -Force }
    Expand-Archive -Path $zipPath -DestinationPath $extractRoot -Force

    $extracted = Get-ChildItem $extractRoot -Directory | Where-Object {
        Test-Path (Join-Path $_.FullName "bin\catalina.bat")
    } | Select-Object -First 1

    if (-not $extracted) {
        throw "Tai Tomcat thanh cong nhung khong tim thay catalina.bat trong zip."
    }

    if (Test-Path $targetHome) { Remove-Item $targetHome -Recurse -Force }
    Move-Item $extracted.FullName $targetHome
    Remove-Item $extractRoot -Recurse -Force -ErrorAction SilentlyContinue
    Remove-Item $zipPath -Force -ErrorAction SilentlyContinue

    Write-Host "Tomcat portable ready: $targetHome" -ForegroundColor Green
    return $targetHome
}

$tomcatHome = Ensure-Tomcat
$env:CATALINA_HOME = $tomcatHome
$env:CATALINA_BASE = $baseDir

$javaHome = Find-JavaHome
if (-not $javaHome) {
    throw "Khong tim thay JDK 17. Cai bang: winget install --id Microsoft.OpenJDK.17 -e"
}
$env:JAVA_HOME = $javaHome
Write-Host "JAVA_HOME=$javaHome" -ForegroundColor DarkGray
Write-Host "CATALINA_HOME=$tomcatHome" -ForegroundColor DarkGray

if ($Action -eq "stop") {
    if (Test-Path $baseDir) {
        & (Join-Path $tomcatHome "bin\catalina.bat") stop
    } else {
        Write-Host "Tomcat chua duoc khoi tao cho du an nay."
    }
    exit $LASTEXITCODE
}

if (Test-Path $baseDir) {
    Clear-StaleTomcatContexts -BaseDirectory $baseDir
}

$dbEndpoint = Get-ConfiguredDbEndpoint
if ($dbEndpoint -and -not (Test-TcpPort -HostName $dbEndpoint.Host -Port $dbEndpoint.Port)) {
    throw "Khong ket noi duoc MySQL tai $($dbEndpoint.Host):$($dbEndpoint.Port). Hay bat MySQL truoc khi chay app.`n" +
          "Neu dung XAMPP/WAMP/MySQL Installer: start service MySQL, mac dinh port 3306.`n" +
          "Neu DB cua ban khong nam o localhost:3306, set HRMS_DB_URL / HRMS_DB_USER / HRMS_DB_PASSWORD roi chay lai."
}

Write-Host "Building project..." -ForegroundColor Cyan
& (Join-Path $projectDir "mvnw.cmd") clean package "-Dmaven.test.skip=true"
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

Clear-StaleTomcatContexts -BaseDirectory $baseDir

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

throw "Tomcat da khoi dong nhung app chua phan hoi. Kiem tra .runtime\tomcat-base\logs.`n" +
      "Thuong gap: chua tao database HRMS_Demo hoac sai MySQL user/password.`n" +
      "Chay sql/01-schema.sql va sql/02-seed.sql trong MySQL Workbench truoc.`n" +
      "DB login mac dinh: root / mat khau trong HRMS_DB_PASSWORD neu MySQL cua ban co password."

