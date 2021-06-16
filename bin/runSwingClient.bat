@echo off
REM runSwingClient.bat
REM This script runs the NFC client with a Swing GUI as a stand-alone app.
REM You need at least Java 1.2 for this.

REM **************************************************************************
REM Change the following variables to match the paths on your system
REM **************************************************************************

REM Set to the location of JDK1.4 (or higher)
SET JAVA_HOME=C:\java\jdk1.4

REM Set to the location of the top level NFC directory
SET NFC_HOME=C:\nfcchat

REM **************************************************************************
REM **************************************************************************

REM The rest of this should theoretically work untouched

cd %NFC_HOME%
echo Changed directory to %NFC_HOME%

SET CLASSPATH=%NFC_HOME%;%NFC_HOME%\conf;%NFC_HOME%\lib\chatclient_swing.jar
echo CLASSPATH is %CLASSPATH%

echo Starting NFC Client
%JAVA_HOME%\bin\java -DNFC_HOME=%NFC_HOME% -DguiFactory=com.lyrisoft.chat.client.gui.jfc.SwingGuiFactory com.lyrisoft.chat.client.Main %1 %2 %3
