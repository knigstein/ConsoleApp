@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION

SET APP_HOME=%~dp0
SET GRADLE_VERSION=8.10.2
SET DIST_DIR=%APP_HOME%\.gradle-dist
SET ZIP_FILE=%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip
SET UNPACK_DIR=%DIST_DIR%\gradle-%GRADLE_VERSION%
SET GRADLE_BIN=%UNPACK_DIR%\bin\gradle.bat

IF NOT EXIST "%GRADLE_BIN%" (
  IF NOT EXIST "%DIST_DIR%" mkdir "%DIST_DIR%"
  IF NOT EXIST "%ZIP_FILE%" (
    powershell -Command "Invoke-WebRequest -Uri https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip -OutFile '%ZIP_FILE%'"
  )
  powershell -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%DIST_DIR%' -Force"
)

CALL "%GRADLE_BIN%" %*
