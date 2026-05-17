#Requires -Version 5.1
<#
.SYNOPSIS
    一键启动 pp-trading 项目所有服务
.EXAMPLE
    .\start-all.ps1
#>

. (Join-Path $PSScriptRoot "common.ps1")
Import-EnvFile
Ensure-RuntimeDirs

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  pp-trading Service Manager" -ForegroundColor Cyan
Write-Host "  Starting all services..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 启动 TimescaleDB (Docker)
Write-Host "[1/6] Starting TimescaleDB..." -ForegroundColor Yellow
$dbContainer = "trading-timescaledb"
$running = docker ps --filter "name=$dbContainer" --format "{{.Names}}" 2>$null
if ($running -eq $dbContainer) {
    Write-Host "[OK] TimescaleDB is already running" -ForegroundColor Green
} else {
    $exists = docker ps -a --filter "name=$dbContainer" --format "{{.Names}}" 2>$null
    if ($exists -eq $dbContainer) {
        docker start $dbContainer | Out-Null
        Write-Host "[OK] TimescaleDB started" -ForegroundColor Green
    } else {
        Write-Host "[INFO] Creating TimescaleDB container..." -ForegroundColor Cyan
        docker run -d `
            --name $dbContainer `
            -p 5432:5432 `
            -e POSTGRES_USER=$env:DB_USER `
            -e POSTGRES_PASSWORD=$env:DB_PASSWORD `
            -e POSTGRES_DB=$env:DB_NAME `
            timescale/timescaledb:latest-pg15 | Out-Null
        Write-Host "[OK] TimescaleDB created and started" -ForegroundColor Green
    }
}

# 等待数据库就绪
Write-Host "      Waiting for database..." -NoNewline
for ($i = 0; $i -lt 30; $i++) {
    if ((docker exec $dbContainer pg_isready -U $env:DB_USER 2>$null) -match "accepting connections") {
        Write-Host ""
        Write-Host "[OK] Database is ready" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 1
    Write-Host "." -NoNewline
    if ($i -eq 29) { Write-Host ""; Write-Host "[WARN] Database may not be fully ready" -ForegroundColor Yellow }
}

# 2-6. 启动 Java 和 Node 服务
Write-Host ""
$step = 2
foreach ($svc in $Services | Where-Object { $_.Type -ne "docker" }) {
    Write-Host "[$step/6] Starting $($svc.Name) (port $($svc.Port))..." -ForegroundColor Yellow
    Start-ServiceProcess -Name $svc.Name -Port $svc.Port -Dir $svc.Dir -Command $svc.Command
    Write-Host ""
    $step++
}

# 等待服务就绪
Write-Host "Waiting for services to be ready..." -ForegroundColor Cyan
foreach ($svc in $Services | Where-Object { $_.Type -ne "docker" }) {
    $timeout = if ($svc.Type -eq "node") { 30 } else { 90 }
    Wait-ForService -Name $svc.Name -Port $svc.Port -TimeoutSeconds $timeout | Out-Null
}

# 状态摘要
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Service Status Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Show-ServiceStatus -ExpectRunning $true
Write-Host ""
Write-Host "  Frontend URL:  http://localhost:3000" -ForegroundColor Cyan
Write-Host "  BFF API:       http://localhost:8181/api/web" -ForegroundColor Cyan
Write-Host "  Data API:      http://localhost:8182/api/market-data" -ForegroundColor Cyan
Write-Host "  Backtest API:  http://localhost:8185/api/backtest" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Startup complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
