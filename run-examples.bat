@echo off
setlocal
echo ================================================================================
echo   Compiling and Running IntelliBranch-JAVA Enterprise Example Gallery
echo ================================================================================

if not exist "target\classes" (
    mkdir target\classes
)

javac -d target/classes -cp target/classes src\main\java\com\intellibranch\core\*.java src\main\java\com\intellibranch\routing\*.java src\main\java\com\intellibranch\neurogate\*.java src\main\java\com\intellibranch\training\*.java src\main\java\com\intellibranch\cli\*.java src\main\java\com\intellibranch\examples\*.java src\main\java\com\intellibranch\*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    exit /b %ERRORLEVEL%
)

java -cp target/classes com.intellibranch.examples.ExampleGallery %*
