#!/bin/sh

#JDK  path
#JAVA_HOME="/usr/share/jdk1.8.0_181"

APP_MAIN=com.webank.webase.node.mgr.Application
CLASSPATH='conf/:apps/*:lib/*'

JAVA_OPTS=" -Dfile.encoding=UTF-8"
JAVA_OPTS+=" -Djava.security.egd=file:/dev/./urandom"

tradePortalPID=0

getTradeProtalPID(){
    javaps=`$JAVA_HOME/bin/jps -l | grep $APP_MAIN`
    if [ -n "$javaps" ]; then
        tradePortalPID=`echo $javaps | awk '{print $1}'`
    else
        tradePortalPID=0
    fi
}


startup(){
    getTradeProtalPID
    echo "==============================================================================================="
    if [ $tradePortalPID -ne 0 ]; then
        echo "$APP_MAIN already started(PID=$tradePortalPID)"
        echo "==============================================================================================="
    else
       javaPath=`which java`
       if [ $javaPath"" = "" ];then
           echo "java not found in PATH"
           exit -1
       fi
    
       echo -n "Starting $APP_MAIN ..."
       nohup $JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASSPATH $APP_MAIN >> ./node-mgr.out 2>&1 &
       
       declare -i count=0
       declare -i ok=0
       while true
       do
            if [ $count -gt 20 ]; then
                break
            fi

            getTradeProtalPID
            if [ $tradePortalPID -ne 0 ]; then
                ok=1
                break
            fi

        count=$count+1
        sleep 1
        done

	    if [ $ok = 0 ]; then
	        echo "[Failed]"
	        echo "==============================================================================================="
	    else
	        echo "[Success]"
	        echo "==============================================================================================="
	    fi
	fi
}

startup