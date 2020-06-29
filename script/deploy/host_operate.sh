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
host=
port=22
user=root
password=
node_root=/opt/fisco
use_docker_sdk=yes

####### error code
SUCCESS=0
PARAM_ERROR=2

####### 参数解析 #######
cmdname=$(basename "$0")

# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    $cmdname [-H host] [-P port] [-u user] [-p password] [-n node_root] [-c] [-d] [-h]
    -H     Required, remote host.
    -P     Not required, remote port, default is 22.
    -u     Not required, remote userName, default is root.
    -p     Required, password.
    -n     Node config root directory, default is /opt/fisco
    -c     Use docker command instead of using docker SDK api, default no.
    -d     Use debug model, default no.
    -h     Show help info.
USAGE
    exit ${PARAM_ERROR}
}


while getopts H:P:u:p:n:dch OPT;do
    case ${OPT} in
        H)
            host=$OPTARG
            ;;
        P)
            port=$OPTARG
            ;;
        u)
            user=$OPTARG
            ;;
        p)
            password=$OPTARG
            ;;
        d)
            debug=yes
            ;;
        n)
            node_root="$OPTARG"
            ;;
        c)
            use_docker_sdk="no"
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

echo "Init server, debug:[${debug}] : ${user}@${host}:${port}#${password}..."

if [[ "${debug}"x == "yes"x ]] ; then
  host=
  port=22
  user=root
  password=
fi


function sshExec(){
    ssh -q -o "StrictHostKeyChecking=no" \
    -o "LogLevel=ERROR" \
    -o "UserKnownHostsFile=/dev/null" \
    -o "PubkeyAuthentication=yes" \
    -o "PasswordAuthentication=no" \
     "${user}"@"${host}" -p "${port}" $@
}

function init() {
    if [[ "$host"x == "127.0.0.1"x || "$host"x == "localhost"x ]] ; then
        echo "Initialing local server ....."

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
                cat "${__dir}/host_docker_tcp.sh" | sshExec bash -e -x
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
    else
        echo "Initialing remote server ....."
#        if [[ "$password"x != ""x ]] ; then
#          cmd="yum"
#          # install sshpass
#          case $(uname | tr '[:upper:]' '[:lower:]') in
#            linux*)
#              # GNU/Linux操作系统
#              # Debian(Ubuntu) or RHEL(CentOS)
#              if [[ $(command -v apt) ]]; then
#                  cmd="apt"
#              fi
#
#              # install sshpass for ssh-copy-id
#              if [[ ! $(command -v sshpass) ]]; then
#                  # install ufw
#                  echo "Installing sshpass on node..."
#                  sudo ${cmd} install -y sshpass
#              fi
#              ;;
#            darwin*)
#              cmd="brew"
#              # install sshpass
#              if [[ ! $(command -v sshpass) ]]; then
#                  echo "Installing sshpass on mac ....."
#                  ${cmd} install http://git.io/sshpass.rb
#              fi
#              ;;
#            *)
#              LOG_WARN "Unsupported Windows yet."
#              ;;
#          esac
#
#        fi

        # ssh-keygen
#        if [[ ! -f ~/.ssh/id_rsa.pub ]]; then
#            echo "Executing ssh-keygen ...."
#            sudo ssh-keygen -q -b 4096 -t rsa -N '' -f ~/.ssh/id_rsa
#        fi
#
#
#        if [[ "$password"x != ""x ]] ; then
#          # scp id_rsa.pub to remote`
#          echo "Start ssh-copy-id to remote server..."
#          sudo sshpass -p "${password}" ssh-copy-id -i ~/.ssh/id_rsa.pub -o "StrictHostKeyChecking=no" -o "LogLevel=ERROR" -o "UserKnownHostsFile=/dev/null" ${user}@${host} -p ${port}
#        fi

        # scp node-init.sh to remote and exec
        cat "${__dir}/host_init_shell.sh" | sshExec bash -e -x
        status=($?)
        if [[ $status != 0 ]] ;then
            echo "Remote init node ERROR!!!"
            exit "$status"
        fi
        
        ## config docker listen on tcp:3000
        if [[ "${use_docker_sdk}"x == "yes"x ]] ; then
            cat "${__dir}/host_docker_tcp.sh" | sshExec bash -e -x
            status=($?)
            if [[ $status != 0 ]] ;then
                echo "Config docker list on tcp:3000 ERROR!!!"
                exit "$status"
            fi
        fi

        echo "mkdir node root ${node_root} on remote"
        sshExec "sudo mkdir -p ${node_root} && sudo chown -R ${user} ${node_root} && sudo chgrp -R ${user} ${node_root}"

        echo "Remote host init SUCCESS!!! "
    fi
}

init
exit ${SUCCESS}


