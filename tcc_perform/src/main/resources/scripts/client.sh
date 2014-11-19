#!/bin/bash

PWD=`pwd`
SCRIPTS='scripts'
PWD=${PWD%'/scripts'}
CP="."
for JAR in $PWD/bin/* ; do CP="$CP:$JAR" ; done
for JAR in $PWD/lib/* ; do CP="$CP:$JAR" ; done
CP=$CP:$PWD/conf
export CLASSPATH=$CP
echo $CLASSPATH

nohup java com.netease.backend.tcc.perftest.TccClient >& client.log
