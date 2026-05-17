#Requires -Version 5.1
<#
.SYNOPSIS
    一键关闭 pp-trading 项目所有服务
.EXAMPLE
    .\stop-all.ps1
#>

. (Join-Path $PSScriptRoot "common.ps1")
Ensure-RuntimeDirs

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  pp-trading Service Manager" -ForegroundColor Cyan
Write-Host "  Stopping all services..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1-5. 停止 Java 和 Node 服务
$step = 1
foreach ($svc in $Services | Where-Object { $_.Type -ne "docker" }) {
    Write-Host "[$step/6] Stopping $($svc.Name)..." -ForegroundColor Yellow
    Stop-ServiceByPid -Name $svc.Name -Port $svc.Port
    $step++
}

# 6. 停止 TimescaleDB
Write-Host "[6/6] Stopping TimescaleDB..." -ForegroundColor Yellow
$running = docker ps --filter "name=trading-timescaledb" --format "{{.Names}}" 2>$null
if ($running -eq "trading-timescaledb") {
    Write-Host "      Stopping container..." -NoNewline
    docker stop trading-timescaledb | Out-Null
    Write-Host " [OK]" -ForegroundColor Green
} else {
    Write-Host "      Container not running" -ForegroundColor DarkGray
}

# 清理PID文件
Write-Host ""
Write-Host "Cleaning up PID files..." -ForegroundColor Cyan
Remove-Item (Join-Path $PidsDir "*.pid") -Force -ErrorAction SilentlyContinue

# 状态摘要
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Service Status Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Show-ServiceStatus -ExpectRunning $false
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All services stopped!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
