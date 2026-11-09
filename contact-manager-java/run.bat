@echo off
echo Compiling...
if not exist out mkdir out
javac -encoding UTF-8 -d out src\*.java
if errorlevel 1 (
    echo Compile failed. Make sure JDK 11 or newer is installed ^(javac -version^).
    pause
    exit /b 1
)
echo Starting server at http://localhost:8080  ^(press Ctrl+C to stop^)
java -cp out Main
pause
