#!/usr/bin/env bash

## use sudo to check dependency installed or install it

# tassl depend
TASSL_CMD="${HOME}"/.fisco/tassl

function install(){
    # 系统命令
    command=$1
    # 安装的应用名，如果系统命令不存在，则安装
    app_name=$2

    # install app
    if [[ ! $(command -v "${command}") ]] ;then
        if [[ $(command -v apt) ]]; then
            # Debian/Ubuntu
            echo "Start to check and install ${app_name} on remote Debian system ....."
            sudo dpkg -l | grep -qw "${app_name}" || sudo apt install -y "${app_name}"
        elif [[ $(command -v yum) ]]; then
            ## RHEL/CentOS
            echo "Start to check and install ${app_name} on remote RHEL system ....."
            sudo rpm -qa | grep -qw "${app_name}" || sudo yum install -y "${app_name}"
        fi
    fi
}

# Update DNS
NAME_SERVER=()
function UpdateDNS() {
  file=/etc/resolv.conf
  for var in ${NAME_SERVER[@]};
  do
    [[ "$(grep -i "$var" "$file")" == "" ]] && sudo echo "nameserver $var" >> "$file"
  done
}
#UpdateDNS

# GNU/Linux操作系统
if [[ $(command -v apt) ]]; then
    # Debian/Ubuntu
    sudo apt -y update && sudo dpkg --configure -a
elif [[ $(command -v yum) ]]; then
    # RHEL/CentOS
    install epel-release epel-release
fi

# install rsync for synchronizing node files
install wget wget
install curl curl
install netstat netstat
#install vim vim
#install rsync rsync
#install nslookup bind-utils
# todo install dos2unix

#
#TASSL_CMD="${HOME}"/.fisco/tassl
#check_and_install_tassl(){
#    if [ ! -f "${TASSL_CMD}" ];then
#        echo "Downloading tassl binary ..."
#        if [[ "$(uname)" == "Darwin" ]];then
#            echo "Visual deploy not support Mac!"
#        else
#            # curl -LO https://github.com/FISCO-BCOS/LargeFiles/raw/master/tools/tassl.tar.gz
#            curl -LO https://osp-1257653870.cos.ap-guangzhou.myqcloud.com/FISCO-BCOS/FISCO-BCOS/tools/tassl-1.0.2/tassl.tar.gz
#        fi
#        tar zxvf tassl.tar.gz && rm tassl.tar.gz
#        chmod u+x tassl
#        mkdir -p "${HOME}"/.fisco
#        mv tassl "${HOME}"/.fisco/tassl
#    fi
#}
#check_and_install_tassl
