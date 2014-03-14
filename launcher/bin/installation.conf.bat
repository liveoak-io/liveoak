
REM %BINDIR% points to the directory containing the launcher script.

REM %JBOSS_MODULES_JAR% should point to exactly the jboss-modules.jar path

pushd %BINDIR%..
set "JBOSS_MODULES_JAR=%CD%\target\jboss-modules.jar"
popd

REM %MODULEPATH% should point to the directory root of additional modules

pushd %BINDIR%..\..
set "MODULEPATH=%CD%\modules\target\modules"
popd

REM %JS_CLIENT_DIR% should point to where the javascript clients may be found.

pushd %BINDIR%..\..
set "JS_CLIENT_DIR=%CD%\clients\javascript\src\main\javascript"
popd

REM %CSS_DIR% should point to where CSS files are kept for HTML encoding

pushd %BINDIR%..
set "CSS_DIR=%CD%\src\main\css"
popd

REM %CONSOLE_DIR% should point to where CSS files are kept for HTML encoding

pushd %BINDIR%..\..
set "CONSOLE_DIR=%CD%\console\target\app"
popd


REM %M2_REPO_DIR% should point to a local m2 repository else it will 
REM default to the user's local m2-repo.

REM in development, we rely on a user's own .m2 repo

REM pushd %BINDIR%..\..
REM set "M2_REPO_DIR=%CD%\modules\target\m2-repo"
REM popd

REM Sample JPDA settings for remote socket debugging
REM set "JAVA_OPTS=%JAVA_OPTS% -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"
