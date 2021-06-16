#!/bin/sh
# setenv.sh
# This script sets NFC_HOME, JAVA_HOME and the CLASSPATH.
# It is used by all sh scripts in this directory.

# Note: uncomment and set JAVA_HOME to point to your JDK1.4 installation
#JAVA_HOME=/usr/local/java/jdk1.4

if [ "x$JAVA_HOME" == "x" ]; then
    echo "Please edit JAVA_HOME in the file " `dirname $0`\setenv.sh " and try again"
    exit 1
fi

if [ ! -d $JAVA_HOME ]; then
    echo "Please edit JAVA_HOME in the file " `dirname $0`\setenv.sh " and try again" 
    exit 1
fi

# This should automagically set NFC_HOME.  If it's not working, 
# manually set it to point to the top-level NFC directory
cd `dirname $0`/..
NFC_HOME=`pwd`
JMS=$NFC_HOME/lib/jms.jar
JNDI=$NFC_HOME/lib/jndi.jar

CLASSPATH=$NFC_HOME/lib/chatserver.jar:$NFC_HOME/lib/chatserver_flash.jar:$NFC_HOME/lib/chatclient_full.jar:$NFC_HOME:$JMS:$JNDI

#echo "using JAVA_HOME=$JAVA_HOME"
#echo "using NFC_HOME=$NFC_HOME"
#echo "using CLASSPATH=$CLASSPATH"

export NFC_HOME
export CLASSPATH
export JAVA_HOME
