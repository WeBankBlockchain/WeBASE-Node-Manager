#!/usr/bin/env bash

## use sudo to check dependency installed or install it

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



