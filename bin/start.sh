#!/bin/sh

export LANG=en_US.UTF-8

rotate_log ()
{
    log=$1;
    num=5;
    if [ -n "$2" ]; then
        num=$2
    fi
    if [ -f "$log" ]; then # rotate logs
        while [ $num -gt 1 ]; do
            prev=`expr $num - 1`
            [ -f "$log.$prev" ] && mv "$log.$prev" "$log.$num"
            num=$prev
        done
        mv "$log" "$log.$num";
    fi
}

bin=`dirname "$0"`
JOB_HOME=`cd "$bin/.."; pwd -P`
JOB_CONF_DIR=$JOB_HOME/config

#JOB_LOGS=/opt/data/goldmine/job/logs
JOB_LOGS=${JOB_LOGS:-${JOB_HOME}/logs}
mkdir -p $JOB_LOGS

PID_FOLDER=$JOB_HOME/pids
mkdir -p $PID_FOLDER

# add all lib and config in classpath 
ELE_LIB=$JOB_HOME/lib
CLASSPATH=$JOB_CONF_DIR
for jar in `ls $ELE_LIB/*.jar`
do
      CLASSPATH="$CLASSPATH:""$jar"
done

for jar in `ls $JOB_HOME/*.jar`
do
      CLASSPATH="$CLASSPATH:""$jar"
done


CLASSPATH=$CLASSPATH:$JOB_HOME/config:$JAVA_HOME/lib/tools.jar

JOPTS="-Djob.log.dir=${JOB_LOGS:-${JOB_HOME}/logs} "
JOPTS="$JOPTS -Djob.log.file=${JOB_LOGFILE:-job.log} "
JOPTS="$JOPTS -Djob.root.logger=${JOB_ROOT_LOGGER:-INFO,DRFA} "
JOPTS="$JOPTS -Dkafka.root.logger=${KAFKA_ROOT_LOGGER:-INFO,kafka} "
JOPTS="$JOPTS -Dzookeeper.root.logger=${ZOOKEEPER_ROOT_LOGGER:-ERROR,zookeeper} "
JOPTS="$JOPTS -server -Xms3072m -Xmx3072m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:$JOB_LOGS/gc.log -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$JOB_LOGS/gc_dump"

log=$JOB_LOGS/job.out
rotate_log $log
nohup java $JOPTS -Djava.library.path=$JAVA_LIBRARY_PATH -Djob.home=$JOB_HOME -classpath $CLASSPATH com.sohu.goldmine.AsyncConsumer > "$log" 2>&1 < /dev/null &

# add pid file
echo $!>$PID_FOLDER/job.pid
echo "pid file created successfully"