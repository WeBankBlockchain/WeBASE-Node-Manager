package com.webank.webase.node.mgr.deploy.service.docker;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.VersionProperties;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.deploy.service.AnsibleService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.tools.SshTools;
import com.webank.webase.node.mgr.deploy.service.PathService;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DockerOptionsCmdImpl implements DockerOptions{

    @Autowired private ConstantProperties constant;
    @Autowired
    private VersionProperties versionProperties;
    @Autowired
    private AnsibleService ansibleService;

    @Override
    public boolean checkImageExists(String ip, String imageTag) {
        String imageFullName = getImageRepositoryTag(constant.getDockerRepository(), constant.getDockerRegistryMirror(), imageTag);

        boolean exist = ansibleService.checkImageExists(ip, imageFullName);
        return exist;

//        String dockerListImageCommand = String.format("sudo docker images -a %s | grep -v 'IMAGE ID'", image);
//        Pair<Boolean, String> result = SshTools.execDocker(ip, dockerListImageCommand, sshUser, sshPort, constant.getPrivateKey());
//        if (result.getKey() && StringUtils.isNotBlank(result.getValue())){
//            return true;
//        }
//        return false;
    }

    /**
     * Pull image, maybe same tag but newer.
     * todo use webase version to pull, to use imageTag pull
     * @param ip
     * @param imageTag
     * @param imagePullType default false
     * @param downloadPath temp dir to save file from cdn
     * @return
     */
    @Override
    public void pullImage(String ip, String imageTag, int imagePullType, String downloadPath) {
        log.info("start pullImage ip:{}, imageTage:{}, pullType:{}", ip, imageTag, imagePullType);
        String imageFullName = getImageRepositoryTag(constant.getDockerRepository(),
            constant.getDockerRegistryMirror(), imageTag);
        String webaseVersion = versionProperties.getVersion();
        boolean isExist = ansibleService.checkImageExists(ip, imageFullName);
        if (isExist) {
            log.warn("pullImage jump over for image:{} already exist.", imageFullName);
            return;
        }

        if (DockerImageTypeEnum.PULL_OFFICIAL.getId() == imagePullType) {
            log.info("pullImage from docker hub");
            String dockerPullCommand = String.format("sudo docker pull %s", imageFullName);
            // kill exists docker pull process
//            SshTools.killCommand(ip, dockerPullCommand, sshUser, sshPort, constant.getPrivateKey());
            ExecuteResult result = ansibleService.execDocker(ip, dockerPullCommand);
            if (result.failed()) {
                throw new NodeMgrException(ConstantCode.ANSIBLE_PULL_DOCKER_HUB_ERROR.attach(result.getExecuteOut()));
            }
        } else if (DockerImageTypeEnum.MANUAL.getId() == imagePullType){
            log.info("pullImage by manually load image");
            return;
        } else {
            log.info("pullImage from cdn");
            ansibleService.execPullDockerCdnShell(ip, downloadPath + "/download", imageTag, webaseVersion);

        }
    }


    @Override
    public void run(String ip, String imageTag, String containerName, String chainRootOnHost, int nodeIndex) {
        String fullImageName = getImageRepositoryTag(constant.getDockerRepository(), constant.getDockerRegistryMirror(), imageTag);
        this.stop(ip, containerName);

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
                "--network=host -w=/data %s ", containerName , nodeRootOnHost, yml, sdk, front_log, fullImageName);
        log.info("Host:[{}] run container:[{}].", ip, containerName);
        // SshTools.execDocker(ip,dockerCreateCommand,sshUser,sshPort,constant.getPrivateKey());
        ansibleService.execDocker(ip, dockerCreateCommand);
    }

    @Override
    public void stop(String ip, String containerName) {
        log.info("stop ip:{}, containerName:{}", ip, containerName);
        boolean containerExist = ansibleService.checkContainerExists(ip, containerName);
        if (!containerExist) {
            log.info("stop container jump over, not found container");
            return;
        }
        String dockerRmCommand = String.format("sudo docker rm -f %s ", containerName);
        ExecuteResult result = ansibleService.execDocker(ip, dockerRmCommand);
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.STOP_NODE_ERROR.attach(result.getExecuteOut()));
        }
    }

}

