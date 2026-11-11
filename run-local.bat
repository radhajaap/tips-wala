@echo off
REM Runs the whole site on Windows WITHOUT Docker: http://localhost:8080
REM Needs: JDK 21, Maven, PostgreSQL (database "marketpulse", user "marketpulse"). See documentation\LOCAL_WINDOWS.md
cd /d "%~dp0"
if not exist ".env" ( echo .env file not found & pause & exit /b 1 )
for /f "usebackq eol=# tokens=1,* delims==" %%a in (".env") do set "%%a=%%b"
set "ROOT=%~dp0"
set "ROOT=%ROOT:\=/%"
set "DB_URL=jdbc:postgresql://localhost:5432/marketpulse"
set "DB_USER=marketpulse"
set "PUBLIC_URL=http://localhost:8080"
set "CORS_ORIGINS=http://localhost:8080"
set "SPRING_WEB_RESOURCES_STATIC_LOCATIONS=file:%ROOT%frontend/"
set "SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:%ROOT%config/"
cd backend
call mvn spring-boot:run
pause
