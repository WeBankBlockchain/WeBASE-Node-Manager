#!/usr/bin/env bash

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

type=up
ip=
user=
port=22
src=
dst=
local=no

####### 参数解析 #######
cmdname=$(basename "$0")

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-t up|down ] [-i ip] [-u ssh_user] [-p ssh_port] [-s src] [-d dst] [-l] [-h]
    -t     Required, transfer file by upload or download, only up and down is valid.
    -i     Required, remote server ip.
    -u     Required, SSH user.
    -p     Required, SSH port, default 22.
    -s     Required, scp source files.
    -d     Required, scp destination files.
    -l     Not Required, if set, then src address and dst address both are the same.
    -h     Show help info.
USAGE
    exit 1
}


while getopts t:i:u:p:s:d:lh OPT;do
    case ${OPT} in
        t)
            case $OPTARG in
                up | down )
                    ;;
                *)
                    usage
                    exit 1;
            esac
            type=$OPTARG
            ;;
        i)
            ip=$OPTARG
            ;;
        u)
            user=$OPTARG
            ;;
        p)
            port=$OPTARG
            ;;
        s)
            src=$OPTARG
            ;;
        d)
            dst=$OPTARG
            ;;
        l)
            local=yes
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

if [[ "${type}"x == "up"x ]] ; then
    echo "Upload files from local:[${src}] to remote dst:[${user}@${ip}:${dst}], using port:[${port}]"

    if [[ "$local"x == "yes"x ]] ; then
        sudo cp -rfv ${src} ${dst}
    else
        scp -o "StrictHostKeyChecking=no" -o "LogLevel=ERROR" -o "UserKnownHostsFile=/dev/null" -P ${port} -r ${src} ${user}@${ip}:${dst}
    fi
elif [[ "${type}"x == "down"x ]] ; then
    echo "Download files from remote :[${user}@${ip}:${src}] to local dst:[${dst}], using port:[${port}]"

    if [[ "$local"x == "yes"x ]] ; then
        sudo cp -rfv ${src} ${dst}
    else
        scp -o "StrictHostKeyChecking=no" -o "LogLevel=ERROR" -o "UserKnownHostsFile=/dev/null" -P ${port} -r ${user}@${ip}:${src} ${dst}
    fi
fi



