#!/usr/bin/env bash

MEM_FREE=`awk '($1 == "MemFree:"){print $2/1048576}' /proc/meminfo`
if [ $(echo "$MEM_FREE < 8"|bc) -eq 1 ];
then
    echo 'ERROR: free mem is too low!'
    exit 1
else
    echo 'free mem is ready'
fi

CPU_CORE=`cat /proc/cpuinfo | grep processor | wc -l`
if [ $CPU_CORE -ge 4 ]
    then echo 'CPU is ready'
else
    echo 'ERROR: CPU CORE is too low'
    exit 1
fi


if [[ ! $(command -v docker) ]]; then
    echo "ERROR: cannot find docker cmd!"
    exit 1
fi

TEST_CNT_NAME='webase_test'
CNT_ID=`docker ps -a | grep $TEST_CNT_NAME | awk '{print $1}'`
if [ -n "$CNT_ID" ]
    then echo "remove cnt-->$CNT_ID"
    docker stop $CNT_ID
    docker rm $CNT_ID
fi

TEST_RESULT=`docker run --name $TEST_CNT_NAME hello-world`
if [[ $TEST_RESULT =~ "Hello from Docker" ]]
    then echo 'docker test passed!'
else
    echo "ERROR: docker run failed-->$TEST_RESULT"
    exit 1
fi

CNT_ID=`docker ps -a | grep $TEST_CNT_NAME | awk '{print $1}'`
if [ -n "$CNT_ID" ]
    then echo "remove cnt-->$CNT_ID"
    docker stop $CNT_ID
    docker rm $CNT_ID
fi


if [[ ! $(command -v docker-compose) ]]; then
    echo "ERROR: cannot find docker compose cmd!"
    exit 1
fi

if [[ ! $(command -v netstat) ]]; then
    echo "ERROR: cannot find netstat cmd!"
    exit 1
fi

if [[ ! $(command -v curl) ]]; then
    echo "ERROR: cannot find curl cmd!"
    exit 1
fi

NODE_PORT=$1

if [ -z "$NODE_PORT" ]
    then echo 'igno check port'
else
    STAT=`netstat -pan | grep -E "${NODE_PORT}.*LISTEN"`
    if [[ $STAT =~ "LISTEN" ]]
    then
        echo "ERROR: port is using"
        exit 1
    else
        echo "port is ready"
    fi
fi



echo "all check passed!"
exit 0