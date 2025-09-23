@echo off

set "ATCS_DIR=%~dp0"
set "MAX_MEM=1024M"
REM required minimum java version is 11
set "JAVA=java.exe"
set "JAVA_OPTS=-DFONT_SCALE=1.0 -Dswing.aatext=true"
set "ENV_FILE=%ATCS_DIR%ATCS.env.bat"

if exist "%ENV_FILE%" (
  call "%ENV_FILE%"
) else (
  echo REM set "MAX_MEM=%MAX_MEM%">"%ENV_FILE%"
  echo REM required minimum java version is 11
  echo REM set "JAVA=%JAVA%">>"%ENV_FILE%"
  echo REM set "JAVA_OPTS=%JAVA_OPTS%">>"%ENV_FILE%"
  echo.>>"%ENV_FILE%"
)

start "" "%JAVA%" %JAVA_OPTS% -Xmx%MAX_MEM% -jar "%ATCS_DIR%\ATCS.jar"
