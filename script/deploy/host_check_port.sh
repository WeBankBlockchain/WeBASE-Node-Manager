#!/usr/bin/env bash

## using sudo netstat to check all process port using detail

PORT_NOT_IN_USE=0
PORT_IN_USE=1
PARAM_ERROR=2

portArray2Check=""

####### 参数解析 #######
cmdname=$(basename "$0")

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-p portArray2Check]

    -p     port  array to check, ex: 20200,30300,8535
USAGE
    exit ${PARAM_ERROR}
}


while getopts p:h OPT;do
    case ${OPT} in
        p)
            portArray2Check=${OPTARG//,/ }
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

if [[ ! $(command -v netstat) ]]; then
    echo "ERROR: cannot find netstat cmd!"
    exit ${PARAM_ERROR}
fi

# use shell script for ansible not support | pipe to grep
function checkPortArrayInUse() {
  FLAG=0
  for NODE_PORT in ${portArray2Check[*]}; do
    # use sudo to check all process, if not sudo, please remove it
    STAT=$(sudo netstat -pan | grep -E "${NODE_PORT}.*LISTEN")
    if [[ $STAT =~ "LISTEN" ]]
    then
        echo "ERROR: port ${NODE_PORT} is in use!"
        FLAG=1
    else
        echo "port ${NODE_PORT} is ready"
    fi
  done

  echo "=========check result========="
  if [[ $FLAG -eq 1 ]]; then
    echo "ERROR: port check NOT PASSED!"
    exit ${PORT_IN_USE}
  else
    echo "all port ${portArray2Check} check passed!"
    exit ${PORT_NOT_IN_USE}
  fi
}
checkPortArrayInUse
