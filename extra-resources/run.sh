#!/bin/sh

INSTANCE_NAME="SCNT-CRAWLER"
JAVA_HOME="$JAVA_HOME"
JAVA_OPTS="-Xms512m -Xmx1024m"
APP_HOME="$(pwd)"
APP_LOG="$APP_HOME/logs"
APP_COMMAND=$1
MAIN_CLASS="com.eopcon.crawler.samsungcnt.Launch"

if [ "$JAVA_HOME" != "" ]
then
    EXEC_JAVA="$JAVA_HOME/bin/java"
else
    EXEC_JAVA=java
fi

if [ "$APP_LOG" != "" ]
then
    JAVA_OPTS="-Dapp.log.dir=$APP_LOG $JAVA_OPTS"
fi

if [ "$APP_COMMAND" = "" ]
then
    APP_COMMAND="run"
fi

if [ $# = 0 ] ; then
    echo "Usage: run.sh <run|execute|stop)"
    echo "        run - start $INSTANCE_NAME in the background"
    echo "        stop - stop $INSTANCE_NAME"
fi

if [ "$1" = "run" ] || [ "$1" = "execute" ] ; then
    if [ -f $INSTANCE_NAME.pid ] ; then
            kill -9 `cat $INSTANCE_NAME.pid`
    fi
	echo "---------------------------------------------------------------------------"
	echo "Start script for the application"
	echo 
	echo "Using JAVA_HOME: \"$JAVA_HOME\""
	echo "Using JAVA_OPTS: \"$JAVA_OPTS\""
	echo "Using APP_HOME_DIR: \"$APP_HOME\""
	echo "Using APP_LOG_DIR: \"$APP_LOG\""
	echo "Using APP_COMMAND: \"$APP_COMMAND\""
	echo "Using MAIN_CLASS: \"$MAIN_CLASS\""
	echo "---------------------------------------------------------------------------"
	
	JAVA_OPTS="-Dspring.profiles.active=$APP_COMMAND $JAVA_OPTS"
	
	if [ "$1" = "run" ] ; then
		nohup $EXEC_JAVA -classpath bootstrap.jar -Dapp.home.dir=$APP_HOME $JAVA_OPTS Bootstrap $MAIN_CLASS $APP_COMMAND > /dev/null 2>&1 &
   		echo $! > $INSTANCE_NAME.pid
	fi

	if [ "$1" = "execute" ] ; then
		$EXEC_JAVA -classpath bootstrap.jar -Dapp.home.dir=$APP_HOME $JAVA_OPTS Bootstrap $MAIN_CLASS $APP_COMMAND
	fi

elif [ "$1" = "stop" ] ; then
	if [ -f $INSTANCE_NAME.pid ] ; then
	    echo "Stop $INSTANCE_NAME..."
	    kill -9 `cat $INSTANCE_NAME.pid`
	    rm -f $INSTANCE_NAME.pid
	    rm -f nohup.out
  	else
    	echo "$INSTANCE_NAME.pid file does not exists!"
  	fi
else
    echo "Invalid argument! $@"
    echo "Usage: run.sh (run|execute|stop)"
fi

