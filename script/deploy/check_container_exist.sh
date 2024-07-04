#!/usr/bin/env bash

FOUND=0
NOT_FOUND=1
PARAM_ERROR=2

containerName=""

####### 参数解析 #######
cmdname=$(basename "$0")

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-c containerName]

    -c     chainPath + nodeDir, ex: webasedefault_chainnode0
USAGE
    exit ${PARAM_ERROR}
}


while getopts c:h OPT;do
    case ${OPT} in
        c)
            containerName="$OPTARG"
            ;;
        h)
            usage
            exit ${PARAM_ERROR}
            ;;
        \?)
            usage
            exit ${PARAM_ERROR}
            ;;
    esac
done


# use shell script for ansible not support | pipe to grep
function grepContainer() {
  docker ps | grep "${containerName}"
  if [ $? -ne 0 ] ;then
      echo "grep ${containerName} not found"
      exit ${NOT_FOUND}
  else
      echo "Found ${containerName}"
      exit ${FOUND}
  fi
}
grepContainer



