package com.webank.webase.node.mgr.deploy.service.docker;

import org.springframework.beans.factory.annotation.Autowired;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.SshTools;
import com.webank.webase.node.mgr.deploy.service.PathService;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DockerOptionsCmdImpl implements DockerOptions{

    @Autowired private ConstantProperties constant;

    /**
     * Pull image, maybe same tag but newer.
     *
     * @param ip
     * @param dockerPort
     * @param sshPort
     * @param imageTag
     * @return
     */
    @Override
    public void pullImage(String ip, int dockerPort,String sshUser, int sshPort, String imageTag) {
        String image = getImageRepositoryTag(constant.getDockerRepository(),constant.getDockerRegistryMirror(),imageTag);
        String dockerPullCommand = String.format("sudo docker pull %s",image);
        SshTools.execDocker(ip,dockerPullCommand,sshUser,sshPort,constant.getPrivateKey());
    }

    @Override
    public void run(String ip, int dockerPort, String sshUser, int sshPort, String imageTag, String containerName, String chainRootOnHost, int nodeIndex) {
        String image = getImageRepositoryTag(constant.getDockerRepository(),constant.getDockerRegistryMirror(),imageTag);
        this.stop(ip,dockerPort,sshUser,sshPort,containerName);

        String nodeRootOnHost = PathService.getNodeRootOnHost(chainRootOnHost, nodeIndex);
        String yml = String.format("%s/application.yml", nodeRootOnHost);
        String sdk = String.format("%s/sdk", chainRootOnHost);
        String front_log = String.format("%s/front-log", nodeRootOnHost);

        String dockerCreateCommand = String.format("sudo docker run -d --rm --name %s " +
                "-v %s:/data " +
                "-v %s:/front/conf/application-docker.yml " +
                "-v %s:/data/sdk " +
                "-v %s:/front/log " +
                "-e SPRING_PROFILES_ACTIVE=docker " +
                "--network=host -w=/data %s ", containerName , nodeRootOnHost, yml,sdk,front_log, image);
        log.info("Host:[{}] run container:[{}].", ip, containerName);
        SshTools.execDocker(ip,dockerCreateCommand,sshUser,sshPort,constant.getPrivateKey());
    }

    @Override
    public void stop(String ip, int dockerPort, String sshUser, int sshPort, String containerName) {
        String dockerRmCommand = String.format("sudo docker rm -f %s ", containerName);
        SshTools.execDocker(ip,dockerRmCommand,sshUser,sshPort,constant.getPrivateKey());
    }
}

