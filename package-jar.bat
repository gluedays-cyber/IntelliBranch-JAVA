@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   IntelliBranch-JAVA: Production Packaging Script
echo ============================================================

set JAR_CMD=jar
where jar >nul 2>nul
if %errorlevel% neq 0 (
    if exist "%JAVA_HOME%\bin\jar.exe" (
        set JAR_CMD="%JAVA_HOME%\bin\jar.exe"
    ) else if exist "C:\Program Files\Java\jdk-26.0.1\bin\jar.exe" (
        set JAR_CMD="C:\Program Files\Java\jdk-26.0.1\bin\jar.exe"
    ) else (
        echo [ERROR] jar command not found in PATH or standard JDK directories.
        exit /b 1
    )
)

if not exist "target\classes\com\intellibranch\Main.class" (
    echo [INFO] Compiling classes...
    if not exist "target\classes" mkdir target\classes
    javac -d target\classes -sourcepath src\main\java src\main\java\com\intellibranch\Main.java
    if %errorlevel% neq 0 (
        echo [ERROR] Compilation failed.
        exit /b 1
    )
)

echo [INFO] Creating distribution JAR: target\intellibranch-3.0.0.jar ...
%JAR_CMD% --create --file target\intellibranch-3.0.0.jar --main-class com.intellibranch.Main -C target\classes com

if %errorlevel% equ 0 (
    echo [SUCCESS] Package created: target\intellibranch-3.0.0.jar
) else (
    echo [ERROR] Failed to package JAR.
    exit /b 1
)
