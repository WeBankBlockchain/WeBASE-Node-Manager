#!/usr/bin/env bash

####### error code
SUCCESS=0
DOCKER_FAIL=5

## use sudo to start docker
function checkDocker(){
  echo "Check docker active"
  # install docker
  # todo install docker by user: centos auto install might go wrong
  if [[ ! $(command -v docker) ]]; then
      echo "Install docker..."
      bash <(curl -s -L get.docker.com)
  fi

  # check docker started
  if [[ $(command -v systemctl) ]]; then
    if [[ "$(systemctl is-active docker)"x != "active"x ]] ; then
      echo "Start docker..."
      sudo systemctl start docker
    fi
  else
    service docker status | grep "running"
    status=($?)
    if [[  $status -eq 0 ]] ; then
      echo "Start docker..."
      sudo service docker start
    fi
  fi
}
checkDocker

function helloDocker() {
  TEST_CNT_NAME='webase_test'
  CNT_ID=`docker ps -a | grep $TEST_CNT_NAME | awk '{print $1}' 2>&1` || :
  if [ -n "$CNT_ID" ]
      then echo "1. remove cnt-->$CNT_ID"
      docker stop $CNT_ID
      docker rm $CNT_ID
  fi

  TEST_RESULT=`docker run --name $TEST_CNT_NAME hello-world 2>&1` || :
  if [[ $TEST_RESULT =~ "Hello from Docker" ]]
      then echo '2. docker run test passed!'
  else
      echo "ERROR: docker run failed-->$TEST_RESULT"
      exit ${DOCKER_FAIL}
  fi

  CNT_ID=`docker ps -a | grep $TEST_CNT_NAME | awk '{print $1}' 2>&1` || :
  if [ -n "$CNT_ID" ]
      then echo "3. after run and remove cnt-->$CNT_ID"
      docker stop $CNT_ID
      docker rm $CNT_ID
  fi
}
helloDocker

echo "docker check passed!"
exit ${SUCCESS}
