@echo off
REM ==========================================
REM DealXanh - Production Startup Script
REM ==========================================

title DealXanh Server

REM --- Cấu hình đường dẫn ---
set "APP_DIR=%~dp0"
set "JAR_FILE=%APP_DIR%DealXanh.jar"
set "CONFIG_DIR=%APP_DIR%config\"
set "UPLOAD_DIR=%APP_DIR%uploads\"

REM --- Java ---
REM Nếu JAVA_HOME chưa set, tự tìm JDK
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-26.0.1" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.1"
        echo JAVA_HOME tu dong set to: %JAVA_HOME%
    )
)
set "JAVA=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA%" (
    echo [LOI] Khong tim thay Java tai: %JAVA%
    echo Vui long cai dat JDK 17+ hoac cau hinh JAVA_HOME
    pause
    exit /b 1
)
echo Java: %JAVA%
echo.

REM --- Tao thu muc uploads neu chua co ---
if not exist "%UPLOAD_DIR%" mkdir "%UPLOAD_DIR%"

REM --- Chay ung dung ---
REM Giai thich tham so:
REM   -Xms256m -Xmx512m    : JVM memory (dieu chinh theo RAM may)
REM   --enable-native-access=ALL-UNNAMED : Fix warning tren Java 20+
REM   --spring.config.additional-location : Load config tu file external

cd /d "%APP_DIR%"

"%JAVA%" ^
    -Xms256m -Xmx512m ^
    --enable-native-access=ALL-UNNAMED ^
    -Dfile.encoding=UTF-8 ^
    -Dspring.config.additional-location=file:./config/ ^
    -jar "%JAR_FILE%"

pause
