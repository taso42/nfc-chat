@echo off
REM runFlashServer.bat
REM This script starts the NFC chat server for Flash clients.
REM Note that if you run this server, you can not use the Java clients.

REM **************************************************************************
REM Change the following variables to match the paths on your system
REM **************************************************************************

REM Set to the location of JDK1.4 (or higher)
SET JAVA_HOME=C:\java\jdk1.4

REM Set to the location of the top level NFC directory
SET NFC_HOME=C:\nfcchat

REM (OPTIONAL) Set the full path of your JMS Implementation
REM This is only required if you are participating in a distributed chat network
SET JMS_IMPL=

REM **************************************************************************
REM **************************************************************************
REM The rest of this should theoretically work untouched

cd %NFC_HOME%
echo Changed directory to %NFC_HOME%

SET CLASSPATH=%NFC_HOME%;%NFC_HOME%\conf;%NFC_HOME%\lib\chatserver_flash.jar;%NFC_HOME%\lib\jndi.jar;%NFC_HOME%\lib\jms.jar;%JMS_IMPL%
echo CLASSPATH is %CLASSPATH%

echo Starting NFC Server
%JAVA_HOME%\bin\java -DNFC_HOME=%NFC_HOME% com.ajdigital.chat.server.remote.FlashChatServer %1 %2 %3

