@echo off

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

set BINDIR=%DIRNAME%

pushd %DIRNAME%..
set "ETCDIR=%CD%\etc"
popd

pushd %DIRNAME%..
set "LIVEOAK=%CD%"
popd

pushd %DIRNAME%..
set "LOGDIR=%CD%\logs"
popd

if "x%INSTALLATION_CONF%" == "x" (
  set "INSTALLATION_CONF=%BINDIR%installation.conf.bat"
)

if exist "%INSTALLATION_CONF%" (
  echo Calling "%INSTALLATION_CONF%"
  call "%INSTALLATION_CONF%"
) else (
  echo Config file not found "%INSTALLATION_CONF%"
)

if "x%M2_REPO_DIR%" == "x" (
  set M2_REPO_CLAUSE=
) else (
  set "M2_REPO_CLAUSE=-Dlocal.maven.repo.path=%M2_REPO_DIR%"
)

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

set JAVA_OPTS=%JAVA_OPTS%

:RESTART
"%JAVA%" %JAVA_OPTS% ^
 "-Dio.liveoak.js.dir=%JS_CLIENT_DIR%" ^
 "-Dconsole.dir=%CONSOLE_DIR%" ^
 "-Dcss.dir=%CSS_DIR%" ^
 "-Dio.liveoak.log=%LOGDIR%" ^
 "-Dlogging.configuration=file:%ETCDIR%\logging.properties" ^
 %M2_REPO_CLAUSE% ^
    -jar "%JBOSS_MODULES_JAR%" ^
    -modulepath "%MODULEPATH%" ^
     io.liveoak.bootstrap:main "%LIVEOAK%" ^
     %*

if ERRORLEVEL 10 goto RESTART

:END
if "x%NOPAUSE%" == "x" pause

:END_NO_PAUSE
