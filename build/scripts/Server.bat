@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Server startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and SERVER_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\GameServer.jar;%APP_HOME%\lib\javax.servlet-api-4.0.1.jar;%APP_HOME%\lib\javax.ws.rs-api-2.1-m05.jar;%APP_HOME%\lib\junit-jupiter-api-5.6.1.jar;%APP_HOME%\lib\mail.jar;%APP_HOME%\lib\netty-all-4.1.0.Final.jar;%APP_HOME%\lib\protobuf-java-3.0.0-beta-3.jar;%APP_HOME%\lib\mongodb-driver-3.3.0.jar;%APP_HOME%\lib\istack-commons-runtime-2.2.jar;%APP_HOME%\lib\jackson-databind-2.9.0.jar;%APP_HOME%\lib\jackson-dataformat-yaml-2.9.0.jar;%APP_HOME%\lib\jackson-core-2.9.0.jar;%APP_HOME%\lib\commons-io-2.5.jar;%APP_HOME%\lib\commons-lang3-3.4.jar;%APP_HOME%\lib\commons-net-3.5.jar;%APP_HOME%\lib\httpclient-4.5.2.jar;%APP_HOME%\lib\poi-3.15-beta1.jar;%APP_HOME%\lib\commons-beanutils-1.9.3.jar;%APP_HOME%\lib\kafka_2.11-0.10.0.0.jar;%APP_HOME%\lib\zkclient-0.8.jar;%APP_HOME%\lib\zookeeper-3.4.6.jar;%APP_HOME%\lib\slf4j-log4j12-1.7.21.jar;%APP_HOME%\lib\log4j-1.2.17.jar;%APP_HOME%\lib\jedis-4.2.2.jar;%APP_HOME%\lib\gson-2.8.9.jar;%APP_HOME%\lib\json-simple-1.1.jar;%APP_HOME%\lib\jaxb-api-2.2.4.jar;%APP_HOME%\lib\mail-1.4.7.jar;%APP_HOME%\lib\mongodb-driver-core-3.3.0.jar;%APP_HOME%\lib\bson-3.3.0.jar;%APP_HOME%\lib\stax-api-1.0.1.jar;%APP_HOME%\lib\activation-1.1.jar;%APP_HOME%\lib\jackson-annotations-2.9.0.jar;%APP_HOME%\lib\snakeyaml-1.17.jar;%APP_HOME%\lib\httpcore-4.4.4.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-codec-1.10.jar;%APP_HOME%\lib\commons-collections-3.2.2.jar;%APP_HOME%\lib\metrics-core-2.2.0.jar;%APP_HOME%\lib\scala-parser-combinators_2.11-1.0.4.jar;%APP_HOME%\lib\scala-library-2.11.8.jar;%APP_HOME%\lib\kafka-clients-0.10.0.0.jar;%APP_HOME%\lib\jopt-simple-4.9.jar;%APP_HOME%\lib\slf4j-api-1.7.32.jar;%APP_HOME%\lib\commons-pool2-2.11.1.jar;%APP_HOME%\lib\json-20211205.jar;%APP_HOME%\lib\stax-api-1.0-2.jar;%APP_HOME%\lib\lz4-1.3.0.jar;%APP_HOME%\lib\snappy-java-1.1.2.4.jar;%APP_HOME%\lib\jline-0.9.94.jar;%APP_HOME%\lib\netty-3.7.0.Final.jar;%APP_HOME%\lib\junit-3.8.1.jar

@rem Execute Server
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %SERVER_OPTS%  -classpath "%CLASSPATH%" games.core.application.GameServer %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable SERVER_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%SERVER_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
