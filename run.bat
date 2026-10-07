@echo off
setlocal
cd /d "%~dp0"

if exist "tools\jdk-*" for /d %%D in ("tools\jdk-*") do set "JAVA_HOME=%%D"
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

where javac >nul 2>nul || (
  echo Java JDK not found. Put a JDK in tools\ or install one.
  pause & exit /b 1
)

dir /b /s src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt || (pause & exit /b 1)
java -cp bin game.Main %*
pause
