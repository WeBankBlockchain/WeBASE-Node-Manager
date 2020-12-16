#!/usr/bin/env bash

# todo check port in use
# todo install netstat or lsof -i:{port}

####### error code
SUCCESS=0
PARAM_ERROR=2
MEM_ERROR=3
CPU_ERROR=4

## default one host, one node+front
node_count=1

####### 参数解析 #######
cmdname=$(basename "$0")

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-C node_count]

    -C     Node count in single host
USAGE
    exit ${PARAM_ERROR}
}


while getopts C:h OPT;do
    case ${OPT} in
        C)
            node_count="$OPTARG"
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

# 通过剩余可用的内存+节点数，单节点至少1G
function checkMem(){
  MEM_FREE=$(awk '($1 == "MemFree:"){print $2/1048576}' /proc/meminfo 2>&1)
  # todo one node+front needs 0.5G
  if [[ $(echo "$MEM_FREE > ${node_count}"|bc) -eq 1 ]];
  then
      echo 'free mem is ready'
  else
      echo 'ERROR: free mem is too low!'
      exit ${MEM_ERROR}
  fi
}
checkMem

function checkCpu() {
  least_core=2
  # 3个及以上节点，需要4核
  if [[ ${node_count} -ge 3 ]]
      then ${least_core}=4
  fi
  # 8个及以上节点，需要8核
#  if [[ ${node_count} -ge 8 ]]
#      then ${least_core}=8
#  fi
  CPU_CORE=$(cat /proc/cpuinfo | grep processor | wc -l 2>&1)
  if [[ $CPU_CORE -ge ${least_core} ]]
      then echo 'CPU is ready'
  else
      echo "ERROR: CPU CORE is too low(at least ${least_core} core)"
      exit ${CPU_ERROR}
  fi
}
checkCpu

echo "all check passed!"
exit ${SUCCESS}