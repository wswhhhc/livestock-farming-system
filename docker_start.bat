@echo off
chcp 65001 >nul

echo ====================================
echo   Livestock Management - Docker Start
echo ====================================
echo.

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker not found. Please install Docker Desktop first.
    echo Download: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)

docker info >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running. Please start Docker Desktop.
    pause
    exit /b 1
)

set /p DB_USERNAME=Enter DB username (default root):
if "%DB_USERNAME%"=="" set DB_USERNAME=root

set /p DB_PASSWORD=Enter DB password (default 252629):
if "%DB_PASSWORD%"=="" set DB_PASSWORD=252629

echo.
echo [1/3] Starting containers (first build takes 3-5 min)...
docker compose up -d --build

echo.
echo [2/3] Waiting for backend...
:retry_be
timeout /t 3 /nobreak >nul
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8088/api/categories' -UseBasicParsing -TimeoutSec 3; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>nul
if %errorlevel% neq 0 (
    goto retry_be
)

echo.
echo ====================================
echo   All services are running!
echo ====================================
echo.
echo   Frontend: http://localhost:5173
echo   Backend:  http://localhost:8088
echo   MySQL:    localhost:3307
echo.
echo   Default login: admin / admin123
echo.
echo   To stop: run stop.bat
echo ====================================
echo.
pause
