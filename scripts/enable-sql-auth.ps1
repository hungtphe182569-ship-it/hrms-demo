# Run this script in an elevated PowerShell (Run as Administrator).
# Enables SQL authentication for local SQLEXPRESS so the HRMS app can connect.

$ErrorActionPreference = "Stop"

Write-Host "Enabling SQL Server Mixed Mode + restarting SQLEXPRESS..." -ForegroundColor Cyan

sqlcmd -S "localhost,1433" -E -Q @"
EXEC xp_instance_regwrite N'HKEY_LOCAL_MACHINE', N'Software\Microsoft\MSSQLServer\MSSQLServer', N'LoginMode', REG_DWORD, 2;
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'hrms_app')
  CREATE LOGIN hrms_app WITH PASSWORD = N'HrmsApp@123', CHECK_POLICY = OFF;
ELSE
  ALTER LOGIN hrms_app WITH PASSWORD = N'HrmsApp@123', CHECK_POLICY = OFF;
ALTER LOGIN hrms_app ENABLE;
USE HRMS_Demo;
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'hrms_app')
BEGIN
  CREATE USER hrms_app FOR LOGIN hrms_app;
  ALTER ROLE db_owner ADD MEMBER hrms_app;
END
"@

Restart-Service 'MSSQL$SQLEXPRESS' -Force
Start-Sleep -Seconds 8

$sqlAuth = sqlcmd -S "localhost,1433" -U hrms_app -P "HrmsApp@123" -Q "SELECT 'SQL_AUTH_OK' AS status;" -h -1 -W
if ("$sqlAuth" -match "SQL_AUTH_OK") {
    Write-Host "SQL auth OK. Now run: .\run.ps1" -ForegroundColor Green
} else {
    Write-Host "SQL auth still failing. Check output above." -ForegroundColor Red
    Write-Host $sqlAuth
    exit 1
}
