#!/bin/sh

bin=`dirname "$0"`
JOB_HOME=`cd "$bin/.."; pwd -P`
if [ "$1" != "" ]; then
	JOB_HOME=$1
fi
PID_FILE=$JOB_HOME/pids/job.pid

PID=`cat $PID_FILE`
if [ -f "$PID_FILE" ]; then 
	`cat "$PID_FILE" | xargs kill -9`
	echo "kill the old server successfully"

fi 