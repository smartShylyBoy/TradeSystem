$ErrorActionPreference = "Continue"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$RuntimeDir = Join-Path $PSScriptRoot ".runtime"
$LogsDir = Join-Path $RuntimeDir "logs"
$PidsDir = Join-Path $RuntimeDir "pids"

$Services = @(
    @{ Name = "TimescaleDB";         Port = 5432; Type = "docker" }
    @{ Name = "market-data-service"; Port = 8182; Type = "java"; Dir = "market-data-service"; Command = "mvn spring-boot:run" }
    @{ Name = "indicator-service";   Port = 8183; Type = "java"; Dir = "indicator-service";   Command = "mvn spring-boot:run" }
    @{ Name = "web-service";         Port = 8181; Type = "java"; Dir = "web-service";         Command = "mvn spring-boot:run" }
    @{ Name = "web-app";             Port = 3000; Type = "node"; Dir = "web-app";             Command = "npm run dev" }
)

function Test-Port {
    param([int]$Port)
    $client = [System.Net.Sockets.TcpClient]::new()
    try { $client.Connect("localhost", $Port); $true }
    catch { $false }
    finally { $client.Dispose() }
}

function Import-EnvFile {
    $envFile = Join-Path $ProjectRoot ".env"
    if (Test-Path $envFile) {
        Get-Content $envFile | ForEach-Object {
            if ($_ -match "^([^#][^=]+)=(.+)$") {
                [Environment]::SetEnvironmentVariable($Matches[1].Trim(), $Matches[2].Trim(), "Process")
            }
        }
        Write-Host "[OK] Loaded .env file" -ForegroundColor Green
    }
}

function Ensure-RuntimeDirs {
    New-Item -ItemType Directory -Force -Path $PidsDir | Out-Null
    New-Item -ItemType Directory -Force -Path $LogsDir | Out-Null
}

function Start-ServiceProcess {
    param(
        [string]$Name,
        [int]$Port,
        [string]$Dir,
        [string]$Command
    )
    if (Test-Port -Port $Port) {
        Write-Host "[OK] $Name is already running on port $Port" -ForegroundColor Green
        return
    }
    $moduleDir = Join-Path $ProjectRoot $Dir
    if (!(Test-Path $moduleDir)) {
        Write-Host "[SKIP] $Name directory not found" -ForegroundColor DarkGray
        return
    }
    $logFile = Join-Path $LogsDir "$Name.log"
    $process = Start-Process -FilePath "cmd" `
        -ArgumentList "/c", "cd /d `"$moduleDir`" && $Command > `"$logFile`" 2>&1" `
        -WindowStyle Hidden `
        -PassThru
    $process.Id | Out-File -FilePath (Join-Path $PidsDir "$Name.pid") -NoNewline
    Write-Host "[OK] $Name starting (PID: $($process.Id))" -ForegroundColor Green
    Write-Host "     Log: $logFile" -ForegroundColor Gray
}

function Stop-ServiceByPid {
    param(
        [string]$Name,
        [int]$Port
    )
    $pidFile = Join-Path $PidsDir "$Name.pid"
    if (Test-Path $pidFile) {
        $svcPid = (Get-Content $pidFile -ErrorAction SilentlyContinue).Trim()
        if ($svcPid -and $svcPid -match '^\d+$') {
            $proc = Get-Process -Id ([int]$svcPid) -ErrorAction SilentlyContinue
            if ($proc) {
                Write-Host "      Stopping $Name (PID: $svcPid)..." -NoNewline
                Stop-Process -Id ([int]$svcPid) -Force -ErrorAction SilentlyContinue
                Start-Sleep -Seconds 1
                if (!(Get-Process -Id ([int]$svcPid) -ErrorAction SilentlyContinue)) {
                    Write-Host " [OK]" -ForegroundColor Green
                } else {
                    Write-Host " [WARN]" -ForegroundColor Yellow
                }
            } else {
                Write-Host "      $Name PID $svcPid already exited" -ForegroundColor DarkGray
            }
        }
        Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
    }
    if ($Port -and (Test-Port -Port $Port)) {
        $portProc = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue |
                    Select-Object -First 1 -ExpandProperty OwningProcess
        if ($portProc) {
            $procName = (Get-Process -Id $portProc -ErrorAction SilentlyContinue).ProcessName
            Write-Host "      Killing process on port $Port (PID: $portProc, $procName)..." -NoNewline
            Stop-Process -Id $portProc -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 1
            Write-Host " [OK]" -ForegroundColor Green
        }
    }
}

function Show-ServiceStatus {
    param([bool]$ExpectRunning = $true)
    foreach ($svc in $Services) {
        $running = Test-Port -Port $svc.Port
        $label = if ($running) { "[RUNNING]" } else { "[STOPPED]" }
        if ($ExpectRunning) {
            $color = if ($running) { "Green" } else { "Red" }
        } else {
            $color = if ($running) { "Red" } else { "Green" }
        }
        $portStr = "port $($svc.Port)".PadRight(10)
        Write-Host "  $($svc.Name.PadRight(22)) $portStr $label" -ForegroundColor $color
    }
}

function Wait-ForService {
    param(
        [string]$Name,
        [int]$Port,
        [int]$TimeoutSeconds = 60
    )
    $elapsed = 0
    while ($elapsed -lt $TimeoutSeconds) {
        if (Test-Port -Port $Port) {
            Write-Host "[OK] $Name is ready on port $Port" -ForegroundColor Green
            return $true
        }
        Start-Sleep -Seconds 1
        $elapsed++
        Write-Host "." -NoNewline
    }
    Write-Host ""
    Write-Host "[WARN] $Name startup timeout after ${TimeoutSeconds}s" -ForegroundColor Yellow
    return $false
}
