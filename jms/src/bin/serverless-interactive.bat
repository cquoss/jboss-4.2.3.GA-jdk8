@echo off
REM This script expects to find either JAVA_HOME set or java in PATH
REM You can set JAVA_HOME here.

SET MAIN_CLASS=org.clester.Main

IF NOT "%1" == "-help" GOTO help

IF "%JAVA_HOME%" == "" (
	SET JAVA="java" 
) ELSE (
	SET JAVA="%JAVA_HOME%\bin\java"
)

SET CP=..\etc;..\lib\jboss-@module.name@.jar;..\lib\jboss-j2ee.jar;..\lib\jboss-common.jar;..\lib\log4j.jar;..\lib\commons-logging.jar;..\lib\jgroups.jar;..\lib\clester.jar

%JAVA% -classpath %CP% %MAIN_CLASS% org.jboss.jms.serverless.client.Interactive %1 %2 %3 %4 %5 %6 %7
GOTO success

:help

ECHO Usage: %0 [-help]
EXIT 1

:success

