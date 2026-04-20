@echo off
setlocal EnableDelayedExpansion

echo ============================================
echo  Liberando porta 8080...
echo ============================================

set TARGET_PORT=8080
set PID_TO_KILL=

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%TARGET_PORT%" ^| findstr "LISTENING"') do (
    set PID_TO_KILL=%%a
    goto :killPort
)

goto :afterKill

:killPort
echo Encerrando processo !PID_TO_KILL! na porta %TARGET_PORT%...
taskkill /PID !PID_TO_KILL! /F >nul 2>&1
timeout /t 2 /nobreak >nul

:afterKill
set PORT_IN_USE=
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%TARGET_PORT%" ^| findstr "LISTENING"') do (
    set PORT_IN_USE=1
)

set SERVER_PORT=8080
if defined PORT_IN_USE (
    echo [WARN] Porta 8080 ainda ocupada. Iniciando na 8081.
    set SERVER_PORT=8081
)

echo ============================================
echo  Iniciando academic-core-service...
echo  Kafka listener: ATIVO
echo  Profile: local
echo  Porta: %SERVER_PORT%
echo ============================================

set SPRING_PROFILES_ACTIVE=local
set SPRING_KAFKA_LISTENER_AUTO_STARTUP=true

REM Use the same database name that exists in your PostgreSQL instance
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/DB_GESTAO_ESCOLAR
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=root123

echo Datasource URL: %SPRING_DATASOURCE_URL%
echo Datasource User: %SPRING_DATASOURCE_USERNAME%

mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=%SERVER_PORT%
