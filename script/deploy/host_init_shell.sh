#!/usr/bin/env bash

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
            dpkg -l | grep -qw "${app_name}" || apt install -y "${app_name}"
        else
            ## RHEL/CentOS
            echo "Start to check and install ${app_name} on remote RHEL system ....."
            rpm -qa | grep -qw "${app_name}" || yum install -y "${app_name}"
        fi
    fi
}

# Update DNS
NAME_SERVER=()
function UpdateDNS() {
  file=/etc/resolv.conf
  for var in ${NAME_SERVER[@]};
  do
    [[ "$(grep -i "$var" "$file")" == "" ]] && echo "nameserver $var" >> "$file"
  done
}
UpdateDNS

# GNU/Linux操作系统
if [[ $(command -v apt) ]]; then
    # Debian/Ubuntu
    apt -y update && apt -y upgrade
else
    # RHEL/CentOS
    install epel-release epel-release
fi

# install rsync for synchronizing node files
install rsync rsync
install wget wget
install vim vim
install curl curl
install nslookup bind-utils

# install docker
if [[ ! $(command -v docker) || ! $(command -v docker-compose) ]]; then
    # install docker first
    if [[ ! $(command -v docker) ]]; then
        echo "Install docker..."
        bash <(curl -s -L get.docker.com)
    fi

    if [[ ! $(command -v docker-compose) ]]; then
        echo "Install docker-compose..."
        curl -L "https://github.com/docker/compose/releases/download/1.25.5/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        chmod +x /usr/local/bin/docker-compose
    fi

    ## update docker demon
    [ -d "/etc/docker" ] || mkdir "/etc/docker"
    cat << EOF > "/etc/docker/daemon.json"
{
  "hosts": ["unix:///var/run/docker.sock", "tcp://0.0.0.0:3000"]
}
EOF

    if [[ $(command -v systemctl) ]]; then
        ## update docker.service, fix https://stackoverflow.com/questions/44052054/unable-to-start-docker-after-configuring-hosts-in-daemon-json
        cp /lib/systemd/system/docker.service /etc/systemd/system/
        sed -i 's/\ -H\ fd:\/\///g' /etc/systemd/system/docker.service
        systemctl daemon-reload

        # Debian/Ubuntu
        systemctl enable docker
        systemctl restart docker
    else
        # RHEL/CentOS
        service docker restart
        chkconfig docker on
    fi

fi

