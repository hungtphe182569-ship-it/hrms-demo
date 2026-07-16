$ErrorActionPreference = "Stop"

$mysqlBaseCandidates = @(
    "C:\Program Files\MySQL\MySQL Server 8.0",
    "C:\Program Files\MySQL\MySQL Server 8.4"
)

$mysqlBase = $mysqlBaseCandidates |
    Where-Object { Test-Path -LiteralPath (Join-Path $_ "bin\mysqld.exe") } |
    Select-Object -First 1

if (-not $mysqlBase) {
    throw "Khong tim thay MySQL Server 8.x trong C:\Program Files\MySQL"
}

$mysqldExe = Join-Path $mysqlBase "bin\mysqld.exe"
$mysqlExe = Join-Path $mysqlBase "bin\mysql.exe"
$myIni = "C:\ProgramData\MySQL\MySQL Server 8.0\my.ini"
if (-not (Test-Path -LiteralPath $myIni)) {
    throw "Khong tim thay my.ini: $myIni"
}

function Wait-MySqlPort {
    param([int]$TimeoutSeconds = 30)

    for ($i = 0; $i -lt $TimeoutSeconds; $i++) {
        $client = New-Object System.Net.Sockets.TcpClient
        try {
            $async = $client.BeginConnect("127.0.0.1", 3306, $null, $null)
            if ($async.AsyncWaitHandle.WaitOne(1000, $false)) {
                $client.EndConnect($async)
                return $true
            }
        } catch {
        } finally {
            $client.Dispose()
        }
    }
    return $false
}

Write-Host "Stopping MySQL80 service..." -ForegroundColor Cyan
Stop-Service MySQL80 -Force
Start-Sleep -Seconds 5

Write-Host "Starting temporary MySQL without grant checks..." -ForegroundColor Cyan
$proc = Start-Process -FilePath $mysqldExe `
    -ArgumentList @("--defaults-file=$myIni", "--skip-grant-tables", "--bind-address=127.0.0.1", "--console") `
    -WindowStyle Hidden `
    -PassThru

try {
    if (-not (Wait-MySqlPort -TimeoutSeconds 40)) {
        throw "Temporary MySQL did not open port 3306."
    }

    Write-Host "Resetting root password to Root@123456..." -ForegroundColor Cyan
    & $mysqlExe -h 127.0.0.1 -P 3306 -u root --protocol=tcp -e @"
FLUSH PRIVILEGES;
CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED BY 'Root@123456';
ALTER USER 'root'@'localhost' IDENTIFIED BY 'Root@123456';
ALTER USER 'root'@'127.0.0.1' IDENTIFIED BY 'Root@123456';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'127.0.0.1' WITH GRANT OPTION;
FLUSH PRIVILEGES;
"@
    if ($LASTEXITCODE -ne 0) {
        throw "mysql.exe reset command failed."
    }
} finally {
    if ($proc -and -not $proc.HasExited) {
        Write-Host "Stopping temporary MySQL..." -ForegroundColor Cyan
        Stop-Process -Id $proc.Id -Force
        Start-Sleep -Seconds 5
    }
}

Write-Host "Starting MySQL80 service..." -ForegroundColor Cyan
Start-Service MySQL80
Start-Sleep -Seconds 8

Write-Host "Testing new password..." -ForegroundColor Cyan
& $mysqlExe -h 127.0.0.1 -P 3306 -u root --password=Root@123456 --protocol=tcp -e "SELECT USER(), VERSION();"
if ($LASTEXITCODE -ne 0) {
    throw "Password test failed."
}

Write-Host "MySQL root password reset OK: Root@123456" -ForegroundColor Green
Write-Host "Use this in Workbench and project:" -ForegroundColor Green
Write-Host "user: root"
Write-Host "password: Root@123456"

Read-Host "Press Enter to close"
