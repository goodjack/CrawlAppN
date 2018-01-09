@echo off

set TITLE=SAMSUNGCNT-CRAWLER
set JAVA_HOME="%JAVA_HOME%"
set JAVA_OPTS=-Xms512m -Xmx1024m 
set APP_HOME="%cd%"
rem set APP_LOG=%APP_HOME%\logs
set APP_LOG=C:/temp/Logs
set APP_COMMAND=%1
set MAIN_CLASS=com.eopcon.crawler.samsungcnt.Launch

if not "%JAVA_HOME%" == "" (
set EXEC_JAVA="%JAVA_HOME%\bin\java"
) else (
set EXEC_JAVA=java
)

if not "%APP_LOG%" == "" (
set JAVA_OPTS=-Dapp.log.dir="%APP_LOG%" %JAVA_OPTS%
)

if "%APP_COMMAND%" == "" (
set APP_COMMAND=run
)

set JAVA_OPTS=-Dspring.profiles.active="%APP_COMMAND%" %JAVA_OPTS%

echo ---------------------------------------------------------------------------
echo Start script for the application
echo.
echo Using JAVA_HOME: %JAVA_HOME%
echo Using JAVA_OPTS: %JAVA_OPTS%
echo Using APP_HOME_DIR: %APP_HOME%
echo Using APP_LOG_DIR: %APP_LOG%
echo Using APP_COMMAND: %APP_COMMAND%
echo Using MAIN_CLASS: %MAIN_CLASS%
echo ---------------------------------------------------------------------------

TITLE %TITLE%
%EXEC_JAVA% -classpath "bootstrap.jar" -Dfile.encoding=utf-8 -Dapp.home.dir="%APP_HOME%" %JAVA_OPTS% Bootstrap %MAIN_CLASS% %APP_COMMAND%