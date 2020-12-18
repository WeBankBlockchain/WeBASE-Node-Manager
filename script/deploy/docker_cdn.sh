#!/usr/bin/env bash

####### error code
SUCCESS=0
PARAM_ERROR=1
# file not exist
DOWNLOAD_ERROR=

#### input param
# ex: /opt/fisco/download/v1.4.2
download_path="/opt/fisco/download"
webase_version="v1.4.2"

# constant param
# url ex: https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeBASE/releases/download/v1.4.2/docker-fisco-webase.tar

# TODO use ansible+javaCommand to call this
function pullCdn() {
  tar_path="${download_path}/docker-fisco-webase.tar"
  echo "Start download docker image tar of webase:${webase_version}..."
  # check exist
  if [[ -f "${tar_path}" ]];then
    echo "Move existed tar to old one"
    mv $tar_path "${tar_path}_old"
  fi
  # download
  wget -P "$download_path" "https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/WeBASE/releases/download/${webase_version}/docker-fisco-webase.tar"
  echo "Download finish in:[${tar_path}]."
}

function loadTar() {
  echo "Start load docker image from tar..."
  tar_path="${download_path}/docker-fisco-webase.tar"
  # check exist
  if [[ -f ${tar_path} ]];then
    sudo docker load -i $tar_path
    echo "Load finish from [${tar_path}]."
  else
    echo "${tar_path} not found!"
    exit $DOWNLOAD_ERROR
  fi
}


####### 参数解析 #######
cmdname=$(basename "$0")

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-d download_dir] [-v webase_version]

    -d     download docker image tar directory
    -v     webase version to get specific image version
    -h     Show help info.
USAGE
    exit ${PARAM_ERROR}
}


while getopts d:v:h OPT;do
    case ${OPT} in
        d)
            node_count="$OPTARG"
            ;;
        v)
            webase_version="$OPTARG"
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


pullCdn
loadTar
exit ${SUCCESS}

