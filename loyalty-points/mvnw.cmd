@echo off
rem Lightweight Maven Wrapper (script-only) for Windows.
rem Reads .mvn\wrapper\maven-wrapper.properties, downloads Maven once into %USERPROFILE%\.m2\wrapper\dists, then runs it.
setlocal EnableExtensions

set "BASE_DIR=%~dp0"
if "%BASE_DIR:~-1%"=="\" set "BASE_DIR=%BASE_DIR:~0,-1%"
set "PROPS=%BASE_DIR%\.mvn\wrapper\maven-wrapper.properties"
if not exist "%PROPS%" goto noprops

set "DIST_URL="
for /f "usebackq tokens=1,* delims==" %%A in ("%PROPS%") do if /i "%%A"=="distributionUrl" set "DIST_URL=%%B"
if not defined DIST_URL goto nourl

set "URL_BACKSLASH=%DIST_URL:/=\%"
for %%F in ("%URL_BACKSLASH%") do set "DIST_FILE=%%~nxF"
set "DIST_NAME=%DIST_FILE:-bin.zip=%"

set "M2_HOME_DIR=%USERPROFILE%\.m2"
if defined MAVEN_USER_HOME set "M2_HOME_DIR=%MAVEN_USER_HOME%"
set "INSTALL_DIR=%M2_HOME_DIR%\wrapper\dists\%DIST_NAME%"
set "MVN_CMD=%INSTALL_DIR%\%DIST_NAME%\bin\mvn.cmd"

if exist "%MVN_CMD%" goto run

echo Downloading Maven: %DIST_URL% 1>&2
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing -Uri '%DIST_URL%' -OutFile '%INSTALL_DIR%\%DIST_FILE%'; Expand-Archive -LiteralPath '%INSTALL_DIR%\%DIST_FILE%' -DestinationPath '%INSTALL_DIR%' -Force; Remove-Item -LiteralPath '%INSTALL_DIR%\%DIST_FILE%'"
if errorlevel 1 goto fail
if not exist "%MVN_CMD%" goto fail

:run
set "MAVEN_PROJECTBASEDIR=%BASE_DIR%"
call "%MVN_CMD%" %*
exit /b %ERRORLEVEL%

:noprops
echo Missing %PROPS% 1>&2
exit /b 1

:nourl
echo distributionUrl not found in %PROPS% 1>&2
exit /b 1

:fail
echo Could not download or unpack Maven. Check your internet connection. 1>&2
exit /b 1
