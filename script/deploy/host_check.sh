#!/usr/bin/env bash

function checkMem(){}
  MEM_FREE=`awk '($1 == "MemFree:"){print $2/1048576}' /proc/meminfo 2>&1`
  if [ $(echo "$MEM_FREE < 7"|bc) -eq 1 ];
  then
      echo 'ERROR: free mem is too low!'
      exit 1
  else
      echo 'free mem is ready'
  fi
}
checkMem

function checkCpu() {
  CPU_CORE=`cat /proc/cpuinfo | grep processor | wc -l 2>&1`
  if [ $CPU_CORE -ge 4 ]
      then echo 'CPU is ready'
  else
      echo 'ERROR: CPU CORE is too low(at least 4 core)'
      exit 1
  fi
}
checkCpu

function checkDocker() {

  if [[ ! $(command -v docker) ]]; then
      echo "ERROR: cannot find docker cmd!"
      exit 1
  fi

  # stop hello world first
  TEST_CNT_NAME='webase_test'
  CNT_ID=`docker ps -a | grep $TEST_CNT_NAME | awk '{print $1}' 2>&1` || :
  if [ -n "$CNT_ID" ]
      then echo "remove cnt-->$CNT_ID"
      docker stop $CNT_ID
      docker rm $CNT_ID
  fi

  # run test hello world
  TEST_RESULT=`docker run --name $TEST_CNT_NAME hello-world 2>&1` || :
  if [[ $TEST_RESULT =~ "Hello from Docker" ]]
      then echo 'docker test passed!'
  else
      echo "ERROR: docker run failed-->$TEST_RESULT"
      exit 1
  fi

  # stop hello world
  CNT_ID=`docker ps -a | grep $TEST_CNT_NAME | awk '{print $1}' 2>&1` || :
  if [ -n "$CNT_ID" ]
      then echo "remove cnt-->$CNT_ID"
      docker stop $CNT_ID
      docker rm $CNT_ID
  fi
}
checkDocker

# need input param
function checkPort() {
  NODE_PORT=$1

  if [[ ! $(command -v netstat) ]]; then
      echo "ERROR: cannot find netstat cmd!"
      exit 1
  fi

  if [[ ! $(command -v curl) ]]; then
      echo "ERROR: cannot find curl cmd!"
      exit 1
  fi

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
}

echo "all check passed!"
#exit 0