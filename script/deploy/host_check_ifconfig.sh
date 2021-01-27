#!/usr/bin/env bash

## use ipconfig to check node-manager's 127.0.0.1
## if add host use 127.0.0.1, then check local eth0 inet equal with other address

UNIQUE_IP_ADDRESS=0
REPEAT_IP_ADDRESS=1
PARAM_ERROR=2

ipArray2Check=""

####### 参数解析 #######
cmdname=$(basename "$0")

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-p ipArray2Check]

    -p     ip array to check same with 127.0.0.1, ex: {ip1},{ip2}
USAGE
    exit ${PARAM_ERROR}
}


while getopts p:h OPT;do
    case ${OPT} in
        p)
            ipArray2Check=${OPTARG//,/ }
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

if [[ ! $(command -v ifconfig) ]]; then
    echo "ERROR: cannot find ifconfig cmd!"
    exit ${PARAM_ERROR}
fi

# use shell script for ansible not support | pipe to grep
function checkUpArrayEqualLocal() {
  FLAG=0
  for IP_ADDRESS in ${ipArray2Check[*]}; do
    STAT=$(ifconfig | grep "${IP_ADDRESS}")
    if [[ "${STAT}" =~ ${IP_ADDRESS} ]] ;
    then
        echo "ERROR: address ${IP_ADDRESS} is already in use!"
        FLAG=1
    else
        echo "address ${IP_ADDRESS} is ok"
    fi
  done

  echo "=========check result========="
  if [[ $FLAG -eq 1 ]]; then
    echo "ERROR: ip address check NOT PASSED!"
    exit ${REPEAT_IP_ADDRESS}
  else
    echo "all address ${ipArray2Check} check passed!"
    exit ${UNIQUE_IP_ADDRESS}
  fi
}
checkUpArrayEqualLocal
