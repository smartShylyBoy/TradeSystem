#Requires -Version 5.1
<#
.SYNOPSIS
    一键重启 pp-trading 项目所有服务
.EXAMPLE
    .\restart-all.ps1
#>

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  pp-trading Service Manager" -ForegroundColor Cyan
Write-Host "  Restarting all services..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Phase 1: Stopping services..." -ForegroundColor Yellow
Write-Host ""
& (Join-Path $PSScriptRoot "stop-all.ps1")

# 等待端口释放
Write-Host ""
Write-Host "Waiting for ports to be released..." -ForegroundColor Cyan
. (Join-Path $PSScriptRoot "common.ps1")
$retries = 0
while ($retries -lt 10) {
    $allFree = $true
    foreach ($svc in $Services | Where-Object { $_.Type -ne "docker" }) {
        if (Test-Port -Port $svc.Port) { $allFree = $false; break }
    }
    if ($allFree) { break }
    Start-Sleep -Seconds 1
    $retries++
}

Write-Host ""
Write-Host "Phase 2: Starting services..." -ForegroundColor Yellow
Write-Host ""
& (Join-Path $PSScriptRoot "start-all.ps1")
