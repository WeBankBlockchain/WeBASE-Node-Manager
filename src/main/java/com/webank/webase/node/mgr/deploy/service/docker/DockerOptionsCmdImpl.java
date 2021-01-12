package com.webank.webase.node.mgr.deploy.service.docker;

import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;
import com.webank.webase.node.mgr.base.properties.VersionProperties;
import com.webank.webase.node.mgr.base.tools.cmd.ExecuteResult;
import com.webank.webase.node.mgr.deploy.service.AnsibleService;
import com.webank.webase.node.mgr.deploy.service.PathService;
import java.io.File;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class DockerOptionsCmdImpl {

    @Autowired private ConstantProperties constant;
    @Autowired
    private VersionProperties versionProperties;
    @Autowired
    private AnsibleService ansibleService;

    /**
     * Get container's name for node.
     *
     * @param rootDirOnHost
     * @param chainName
     * @param hostIndex
     * @return delete all {@link File#separator} and blank of node path on host.
     */
    public static String getContainerName(String rootDirOnHost, String chainName, int hostIndex) {
        return String.format("%s%snode%s",
            rootDirOnHost.replaceAll(File.separator, "").replaceAll(" ", ""), chainName, hostIndex);
    }


    /**
     *
     * @return
     */
    private String getImageRepositoryTag(String dockerRepository, String dockerRegistryMirror, String imageTag) {
        // image repository and tag
        String image = String.format("%s:%s", dockerRepository, imageTag);
        if (StringUtils.isNotBlank(dockerRegistryMirror)) {
            // image with mirror
            image = String.format("%s/%s", dockerRegistryMirror, image);
        }
        return image;
    }


    public boolean checkImageExists(String ip, String imageTag) {
        String imageFullName = getImageRepositoryTag(constant.getDockerRepository(), constant.getDockerRegistryMirror(), imageTag);

        boolean exist = ansibleService.checkImageExists(ip, imageFullName);
        return exist;

    }

    /**
     * Pull image, maybe same tag but newer.
     * todo: separate fisco and webase version, use webase version to pull, to use imageTag pull
     * @param ip
     * @param imageTag
     * @param imagePullType default false
     * @param downloadPath temp dir to save file from cdn
     * @return
     */
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
            String dockerPullCommand = String.format("docker pull %s", imageFullName);
            // kill exists docker pull process
//            SshTools.killCommand(ip, dockerPullCommand, sshUser, sshPort, constant.getPrivateKey());
            ExecuteResult result = ansibleService.execDocker(ip, dockerPullCommand);
            if (result.failed()) {
                throw new NodeMgrException(ConstantCode.ANSIBLE_PULL_DOCKER_HUB_ERROR.attach(result.getExecuteOut()));
            }
        } else if (DockerImageTypeEnum.MANUAL.getId() == imagePullType){
            log.info("pullImage by manually load image");
            boolean imageExist = this.checkImageExists(ip, imageTag);
            if (imageExist) {
                log.info("image of {} check exist, success", imageTag);
                return;
            } else {
                log.error("not found manually loaded image of :{}", imageTag);
                throw new NodeMgrException(ConstantCode.IMAGE_NOT_EXISTS_ON_HOST.attach(imageTag));
            }
        } else {
            log.info("pullImage from cdn");
            ansibleService.execPullDockerCdnShell(ip, downloadPath + "/download", imageTag, webaseVersion);
        }
    }


    public void run(String ip, String imageTag, String containerName, String chainRootOnHost, int nodeIndex) {
        String fullImageName = getImageRepositoryTag(constant.getDockerRepository(), constant.getDockerRegistryMirror(), imageTag);
        this.stop(ip, containerName);

        String nodeRootOnHost = PathService.getNodeRootOnHost(chainRootOnHost, nodeIndex);
        String yml = String.format("%s/application.yml", nodeRootOnHost);
        String sdk = String.format("%s/sdk", chainRootOnHost);
        String front_log = String.format("%s/front-log", nodeRootOnHost);

        String dockerCreateCommand = String.format("docker run -d --rm --name %s " +
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

    public void stop(String ip, String containerName) {
        log.info("stop ip:{}, containerName:{}", ip, containerName);
        boolean containerExist = ansibleService.checkContainerExists(ip, containerName);
        if (!containerExist) {
            log.info("stop container jump over, not found container");
            return;
        }
        String dockerRmCommand = String.format("docker rm -f %s ", containerName);
        ExecuteResult result = ansibleService.execDocker(ip, dockerRmCommand);
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.STOP_NODE_ERROR.attach(result.getExecuteOut()));
        }
    }

}

