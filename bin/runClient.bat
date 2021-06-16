@echo off
REM runClient.bat
REM This script runs the NFC client with an AWT GUI as a stand-alone app.
REM You can use any Java version for this.

REM **************************************************************************
REM Change the following variables to match the paths on your system
REM **************************************************************************

REM Set to the location of JDK1.0.2 (or higher)
SET JAVA_HOME=C:\java\jdk1.4

REM Set to the location of the top level NFC directory
SET NFC_HOME=C:\nfcchat

REM **************************************************************************
REM **************************************************************************

REM The rest of this should theoretically work untouched

cd %NFC_HOME%
echo Changed directory to %NFC_HOME%

SET CLASSPATH=%NFC_HOME%\conf;%NFC_HOME%\lib\chatclient_awt.jar
echo CLASSPATH is %CLASSPATH%

echo Starting NFC Client
%JAVA_HOME%\bin\java -DNFC_HOME=%NFC_HOME% com.lyrisoft.chat.client.Main %1 %2 %3
