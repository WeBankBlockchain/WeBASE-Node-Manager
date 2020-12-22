#!/usr/bin/env bash

# install sshpass
# ssh-keygen if id_rsa.pub not exists
# upload id_rsa.pub to remote server

LOG_WARN()
{
    local content=${1}
    echo -e "\033[31m[WARN] ${content}\033[0m"
}

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

# 命令返回非 0 时，就退出
set -o errexit
# 管道命令中任何一个失败，就退出
set -o pipefail
# 遇到不存在的变量就会报错，并停止执行
set -o nounset
# 在执行每一个命令之前把经过变量展开之后的命令打印出来，调试时很有用
#set -o xtrace

# 退出时，执行的命令，做一些收尾工作
trap 'echo -e "Aborted, error $? in command: $BASH_COMMAND"; trap ERR; exit 1' ERR

# Set magic variables for current file & dir
# 脚本所在的目录
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 脚本的全路径，包含脚本文件名
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
# 脚本的名称，不包含扩展名
__base="$(basename ${__file} .sh)"
# 脚本所在的目录的父目录，一般脚本都会在父项目中的子目录，
#     比如: bin, script 等，需要根据场景修改
__root="$(cd "$(dirname "${__dir}")" && pwd)" # <-- change this as it depends on your app
# install sshpass

debug=no
node_root=/opt/fisco
use_docker_sdk=no

####### error code
SUCCESS=0
PARAM_ERROR=2

####### 参数解析 #######
cmdname=$(basename "$0")

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-n node_root] [-d] [-h]
    -n     Node config root directory, default is /opt/fisco
    -d     Use debug model, default no.
    -h     Show help info.
USAGE
    exit ${PARAM_ERROR}
}


while getopts n:dh OPT;do
    case ${OPT} in
        d)
            debug=yes
            ;;
        n)
            node_root="$OPTARG"
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

function init() {
    echo "Initialing server ....."

    case $(uname | tr '[:upper:]' '[:lower:]') in
      linux*)
        # GNU/Linux操作系统
        # Debian(Ubuntu) or RHEL(CentOS)
        bash -e -x "${__dir}/host_init_shell.sh"
        status=($?)
        if [[ $status != 0 ]] ;then
            echo "Local init node ERROR!!!"
            exit "$status"
        fi

        ## config docker listen on tcp:3000
        if [[ "${use_docker_sdk}"x == "yes"x ]] ; then
            cat "${__dir}/host_docker_tcp.sh" | bash -e -x
            status=($?)
            if [[ $status != 0 ]] ;then
                echo "Config docker list on tcp:3000 ERROR!!!"
                exit "$status"
            fi
        fi
        ;;
    esac
    echo "mkdir node root ${node_root} on local"
    sudo mkdir -p ${node_root}

    echo "Local host init SUCCESS!!! "

}
init


exit ${SUCCESS}


