#!/usr/bin/env bash

FOUND=0
NOT_FOUND=1
PARAM_ERROR=2

## default one host, one node+front
imageFullName="fiscoorg/fisco-webase:v2.7.0"

####### 参数解析 #######
cmdname=$(basename "$0")

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-i imageName]

    -i     ImageRepo + / + imageName + tagId, ex: fiscoorg/fisco-webase:v2.7.0
USAGE
    exit ${PARAM_ERROR}
}


while getopts i:h OPT;do
    case ${OPT} in
        i)
            imageFullName="$OPTARG"
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
function grepImage() {
  docker images -a "${imageFullName}" | grep -v 'IMAGE ID'
  if [ $? -ne 0 ] ;then
      echo "grep ${imageFullName} not found"
      exit $NOT_FOUND
  else
      echo "Found ${imageFullName}"
      exit $FOUND
  fi
}
grepImage



