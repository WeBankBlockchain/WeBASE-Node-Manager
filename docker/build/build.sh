#!/usr/bin/env bash

LOG_WARN() {
    local content=${1}
    echo -e "\033[31m[WARN] ${content}\033[0m"
}

LOG_INFO() {
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
__root="$(cd "$(dirname "${__dir}")" && pwd)"/../ # <-- change this as it depends on your app
__root=$(realpath -s "${__root}")


########################### properties config ##########################
image_organization=registry.cn-beijing.aliyuncs.com/qchain
image_name="webase-node-mgr3"
docker_push="no"
latest_tag=latest
new_tag=


########################### parse param ##########################
__cmd="$(basename $0)"
# 解析参数
# usage help doc.
usage() {
    cat << USAGE  >&2
Usage:
    ${__cmd}    [-h] [-t new_tag] [-p] [-i fiscoorg]
    -t          New tag for image, required.

    -p          Push image to docker hub, default no.
    -i          Default organization, default webasepro.
    -h          Show help info.
USAGE
    exit 1
}
while getopts t:i:ph OPT;do
    case $OPT in
        t)
            new_tag=$OPTARG
            ;;
        p)
            docker_push=yes
            ;;
        i)
            image_organization=${OPTARG}
            ;;
        h)
            usage
            exit 3
            ;;
        \?)
            usage
            exit 4
            ;;
    esac
done


# 必须设置新镜像的版本
if [[ "${new_tag}"x == "x" ]] ; then
  LOG_WARN "Need a new_tag for new docker image!! "
  usage
  exit 1
fi

########################### build docker image ##########################
image_repository="${image_organization}/${image_name}"

## compile project
cd "${__root}" && chmod +x ./gradlew && ./gradlew clean build -x test

## docker build
cd "${__root}"/dist

cp ../docker/openssl-1.1.1q.tar.gz ./

docker build -t ${image_repository}:${new_tag} -f "${__root}"/docker/build/Dockerfile .
docker tag "${image_repository}:${new_tag}" "${image_repository}:${latest_tag}"


########################### push docker image ##########################
if [[ "${docker_push}"x == "yesx" ]] ; then
    docker push "${image_repository}:${new_tag}"
    docker push "${image_repository}:${latest_tag}"
fi







