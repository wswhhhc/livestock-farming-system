@echo off

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8088 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)

timeout /t 2 /nobreak >nul

cd /d "%~dp0backend"
start "backend" cmd /c mvn spring-boot:run

cd /d "%~dp0frontend"
start "frontend" cmd /c npm run dev
