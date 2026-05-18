@echo off
chcp 65001 >nul

echo ====================================
echo   Stopping Docker containers
echo ====================================
docker compose down
echo.
echo Containers stopped.
echo.
pause
