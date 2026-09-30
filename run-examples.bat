@echo off
setlocal
echo ================================================================================
echo   Compiling and Running IntelliBranch-JAVA Enterprise Example Gallery
echo ================================================================================

if not exist "target\classes" (
    mkdir target\classes
)

powershell -Command "Get-ChildItem -Recurse -Path src/main/java/*.java | ForEach-Object { $_.FullName } | Out-File -Encoding ascii target/sources.txt"

javac -d target/classes -cp target/classes @target/sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    exit /b %ERRORLEVEL%
)

if "%1"=="mega" (
    java -cp target/classes com.intellibranch.examples.enterprise.MegaGalleryRunner %2
) else if "%1"=="verify" (
    java -cp target/classes com.intellibranch.examples.enterprise.MegaGalleryRunner --verify
) else (
    java -cp target/classes com.intellibranch.examples.ExampleGallery %*
)
