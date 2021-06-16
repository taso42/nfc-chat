@echo off
REM runServer.bat
REM This script starts the NFC chat server.

REM **************************************************************************
REM Change the following variables to match the paths on your system
REM **************************************************************************

REM Set to the location of JDK1.4 (or higher)
SET JAVA_HOME=C:\java\jdk1.4.1

REM Set to the location of the top level NFC directory
SET NFC_HOME=C:\nfcchat

REM Set the full path to jms.jar
SET JMS_JAR=%NFC_HOME%\lib\jms.jar

REM Set the full path to JNDI.jar
SET JNDI_JAR=%NFC_HOME%\lib\jndi.jar

REM (OPTIONAL) Set the full path of your JMS Implementation
REM This is only required if you are participating in a distributed chat network
SET JMS_IMPL=

REM **************************************************************************
REM **************************************************************************
REM The rest of this should theoretically work untouched

cd %NFC_HOME%
echo Changed directory to %NFC_HOME%

SET CLASSPATH=%NFC_HOME%;%NFC_HOME%\conf;%NFC_HOME%\lib\chatserver.jar;%JMS_JAR%;%JNDI_jar%;%JMS_IMPL%
echo CLASSPATH is %CLASSPATH%

echo Starting NFC Server
%JAVA_HOME%\bin\java -DNFC_HOME=%NFC_HOME% com.lyrisoft.chat.server.remote.ChatServer %1 %2 %3

