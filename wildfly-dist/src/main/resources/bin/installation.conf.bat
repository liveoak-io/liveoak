
pushd %BINDIR%..
set "MODULEPATH=%CD%\modules"
popd

pushd %BINDIR%..
set "JBOSS_MODULES_JAR=%CD%\lib\jboss-modules.jar"
popd

pushd %BINDIR%..
set "JS_CLIENT_DIR=%CD%\javascript"
popd

pushd %BINDIR%..
set "CSS_DIR=%CD%\css"
popd

pushd %BINDIR%..
set "M2_REPO_DIR=%CD%\m2-repo"
popd
