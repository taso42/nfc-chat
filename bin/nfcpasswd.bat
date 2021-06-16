@echo off
REM nfcpasswd.bat
REM This script adds users to the nfc.passwd file. Run it from the command line and invoke as:
REM nfcpasswd.bat [username] [access level] [password]
REM After this you should see a new line in the conf\nfc.passwd file. [access level] can be
REM any number, it should correspond to your commands.properties file. Normally, you would use
REM 0 for no access, 1 for normal user and 2 for moderator.

REM **************************************************************************
REM Change the following variables to match the paths on your system
REM **************************************************************************

REM Set to the location of JDK1.4 (or higher)
SET JAVA_HOME=C:\java\jdk1.4

REM Set to the location of the top level NFC directory
SET NFC_HOME=C:\nfcchat

REM **************************************************************************
REM The rest of this should theoretically work untouched
REM **************************************************************************

cd %NFC_HOME%
SET CLASSPATH=%NFC_HOME%;%NFC_HOME%\conf;%NFC_HOME%\lib\chatserver.jar;%NFC_HOME%\lib\jms.jar

echo Running Passwd program
%JAVA_HOME%\bin\java -DNFC_HOME=%NFC_HOME% com.lyrisoft.chat.server.remote.auth.Passwd %1 %2 %3

