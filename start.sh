#!/bin/bash

APP_MAIN=com.webank.webase.node.mgr.Application
CLASSPATH='conf/:apps/*:lib/*'
CURRENT_DIR=`pwd`
LOG_DIR=${CURRENT_DIR}/log
CONF_DIR=${CURRENT_DIR}/conf
ARIAL_DIR=${CONF_DIR}/Arial

SERVER_PORT=$(cat $CONF_DIR/application.yml| grep "port" | awk '{print $2}'| sed 's/\r//')
if [ ${SERVER_PORT}"" = "" ];then
    echo "$CONF_DIR/application.yml server port has not been configured"
    exit -1
fi

copyArial(){
    arialFolder="/usr/share/fonts/arial"
    if [ ! -d "$arialFolder" ]; then
        sudo mkdir "$arialFolder" && sudo cp $ARIAL_DIR/arial*.ttf $arialFolder/
    fi
}

mkdir -p log

startWaitTime=600
processPid=0
processStatus=0
server_pid=0
checkProcess(){
    server_pid=`ps aux | grep java | grep $APP_MAIN | awk '{print $2}'`
    port_pid=`netstat -anp 2>&1|grep $SERVER_PORT|awk '{printf $7}'|cut -d/ -f1`
    if [ -n "$port_pid" ] && [ -n "$(echo $port_pid| sed -n "/^[0-9]\+$/p")" ]; then
        if [[ $server_pid =~ $port_pid ]]; then
            processPid=$port_pid
            processStatus=2
        else
            processPid=$port_pid
            processStatus=1
        fi
    else
        processPid=0
        processStatus=0
    fi
}

JAVA_OPTS=" -Dfile.encoding=UTF-8"
JAVA_OPTS+=" -Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS+=" -Xmx256m -Xms256m -Xmn128m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"
JAVA_OPTS+=" -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}/heap_error.log"


start(){
    copyArial
    checkProcess
    echo "==============================================================================================="
    if [ $processStatus == 1 ]; then
        echo "Port $SERVER_PORT has been occupied by other server PID($processPid)"
        echo "==============================================================================================="
    elif [ $processStatus == 2 ]; then
        echo "Server $APP_MAIN Port $SERVER_PORT is running PID($processPid)"
        echo "==============================================================================================="
    else
        echo -n "Starting Server $APP_MAIN Port $SERVER_PORT ..."
        nohup $JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASSPATH $APP_MAIN >> $LOG_DIR/node-manager.out 2>&1 &
        
        count=1
        result=0
        while [ $count -lt $startWaitTime ] ; do
           checkProcess
           if [ $processPid -ne 0 ]; then
               result=1
               break
           fi
           let count++
           echo -n "."
           sleep 1
       done
        
       if [ $result -ne 0 ]; then
           echo "PID($processPid) [Success]"
           echo "==============================================================================================="
       else
           for subPid in ${server_pid[@]} ; do
               checkResult=`netstat -tunpl 2>&1|grep $subPid|awk '{printf $7}'|cut -d/ -f1`
               if [ -z "$checkResult" ]; then
                   kill -9 $subPid
                   message="Because port $SERVER_PORT not up in $startWaitTime seconds.Script finally killed the process."
               fi
           done
           echo "[Failed]. Please view log file (default path:./log/)."
           echo $message
           echo "==============================================================================================="
       fi
    fi
}

start
