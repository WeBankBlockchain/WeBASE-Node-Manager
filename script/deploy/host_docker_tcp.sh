#!/usr/bin/env bash

echo "Config docker to listen on tcp port:[3000]"
case $(uname | tr '[:upper:]' '[:lower:]') in
    linux*)
        # GNU/Linux操作系统
        # Debian(Ubuntu) or RHEL(CentOS)
        # mkdir /etc/docker if not exists
        [ -d "/etc/docker" ] || sduo mkdir "/etc/docker";

        # update docker demon listen tcp on 3000
sduo cat << EOF > "/etc/docker/daemon.json"
{
  "hosts": ["unix:///var/run/docker.sock", "tcp://0.0.0.0:3000"]
}
EOF
        # update docker systemctl config and start docker service
        if [[ $(command -v systemctl) ]]; then
            ## update docker.service, fix https://stackoverflow.com/questions/44052054/unable-to-start-docker-after-configuring-hosts-in-daemon-json
            sudo cp -fv /lib/systemd/system/docker.service /etc/systemd/system/
            sudo sed -i 's/\ -H\ fd:\/\///g' /etc/systemd/system/docker.service

            sudo systemctl daemon-reload
            sudo systemctl enable docker
            sudo systemctl restart docker
        else
            sudo service docker restart
            sudo chkconfig docker on
        fi
        ;;
    darwin*)
      LOG_WARN "Please config docker by yourself on macOS."
      ;;
    *)
      LOG_WARN "Unsupported Windows yet."
      ;;
esac
