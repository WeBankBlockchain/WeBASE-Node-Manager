/**
 * Copyright 2014-2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.deploy.service;

import com.qctc.host.api.model.HostDTO;
import com.webank.webase.node.mgr.base.code.ConstantCode;
import com.webank.webase.node.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.node.mgr.base.exception.NodeMgrException;
import com.webank.webase.node.mgr.config.properties.ConstantProperties;
import com.webank.webase.node.mgr.config.properties.VersionProperties;
import com.webank.webase.node.mgr.deploy.chain.ChainService;
import com.webank.webase.node.mgr.tools.cmd.ExecuteResult;
import java.io.File;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class DockerCommandService {

    @Autowired private ConstantProperties constant;
    @Autowired
    private VersionProperties versionProperties;

    @Lazy
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
        chainName = ChainService.getChainDirName(chainName, "");
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


    public boolean checkImageExists(HostDTO hostDTO, String imageTag) {
        String imageFullName = getImageRepositoryTag(constant.getDockerRepository(), constant.getDockerRegistryMirror(), imageTag);

        boolean exist = ansibleService.checkImageExists(hostDTO, imageFullName);
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
    public void pullImage(HostDTO hostDTO, String imageTag, int imagePullType, String downloadPath) {
        log.info("start pullImage ip:{}, imageTage:{}, pullType:{}", hostDTO.getIp(), imageTag, imagePullType);
        String imageFullName = getImageRepositoryTag(constant.getDockerRepository(),
            constant.getDockerRegistryMirror(), imageTag);
        String webaseVersion = versionProperties.getVersion();
        boolean isExist = ansibleService.checkImageExists(hostDTO, imageFullName);
        if (isExist) {
            log.warn("pullImage jump over for image:{} already exist.", imageFullName);
            return;
        }

        if (DockerImageTypeEnum.PULL_OFFICIAL.getId() == imagePullType) {
            log.info("pullImage from docker hub");
            String dockerPullCommand = String.format("docker pull %s", imageFullName);
            // kill exists docker pull process
            ExecuteResult result = ansibleService.execDockerPull(hostDTO, dockerPullCommand);
            if (result.failed()) {
                throw new NodeMgrException(ConstantCode.ANSIBLE_PULL_DOCKER_HUB_ERROR.attach(result.getExecuteOut()));
            }
        } else if (DockerImageTypeEnum.MANUAL.getId() == imagePullType){
            log.info("pullImage by manually load image");
            boolean imageExist = this.checkImageExists(hostDTO, imageTag);
            if (imageExist) {
                log.info("image of {} check exist, success", imageTag);
                return;
            } else {
                log.error("not found manually loaded image of :{}", imageTag);
                throw new NodeMgrException(ConstantCode.IMAGE_NOT_EXISTS_ON_HOST.attach(imageTag));
            }
        } else {
            log.info("pullImage from cdn");
            ansibleService.execPullDockerCdnShell(hostDTO, downloadPath + "/download", imageTag, webaseVersion);
        }
    }


    public void run(HostDTO hostDTO, String imageTag, String containerName, String chainRootOnHost, int nodeIndex, int cpus, int memory) {
        log.info("stop ip:{}, imageTag:{},containerName:{},chainRootOnHost:{},nodeIndex:{}",
                hostDTO.getIp(), imageTag, containerName, chainRootOnHost, nodeIndex);
        String fullImageName = getImageRepositoryTag(constant.getDockerRepository(), constant.getDockerRegistryMirror(), imageTag);
        this.stop(hostDTO, containerName);

        String nodeRootOnHost = PathService.getNodeRootOnHost(chainRootOnHost, nodeIndex);
        String yml = String.format("%s/application.yml", nodeRootOnHost);
        String sdk = String.format("%s/sdk", chainRootOnHost);
        String front_log = String.format("%s/front-log", nodeRootOnHost);

        if (cpus < 1) cpus = 1;
        if (memory < 1) memory = 1;

        String dockerCreateCommand = String.format("docker run -d --rm --name %s --cpus=%s -m %sGB " +
                "-v %s:/data " +
                "-v %s:/front/conf/application-docker.yml " +
                "-v %s:/data/sdk " +
                "-v %s:/front/log " +
                "-e SPRING_PROFILES_ACTIVE=docker " +
                "--network=host -w=/data %s ", containerName, cpus, memory, nodeRootOnHost, yml, sdk, front_log, fullImageName);
        log.info("Host:[{}] run container:[{}].", hostDTO.getIp(), containerName);
        // SshTools.execDocker(ip,dockerCreateCommand,sshUser,sshPort,constant.getPrivateKey());
        ansibleService.execDocker(hostDTO, dockerCreateCommand);
    }

    public void stop(HostDTO hostDTO, String containerName) {
        log.info("stop ip:{}, containerName:{}", hostDTO.getIp(), containerName);
        boolean containerExist = ansibleService.checkContainerExists(hostDTO, containerName);
        if (!containerExist) {
            log.info("stop container jump over, not found container");
            return;
        }
        String dockerRmCommand = String.format("docker rm -f %s ", containerName);
        ExecuteResult result = ansibleService.execDocker(hostDTO, dockerRmCommand);
        if (result.failed()) {
            throw new NodeMgrException(ConstantCode.STOP_NODE_ERROR.attach(result.getExecuteOut()));
        }
    }

    public String stats(HostDTO hostDTO, String containerName) {
        log.info("stats ip:{}, containerName:{}", hostDTO.getIp(), containerName);
         /*
         boolean containerExist = ansibleService.checkContainerExists(hostDTO, containerName);
         if (!containerExist) {
             log.info("stats container jump over, not found container");
             return null;
         }
         */
        String dockerRmCommand = String.format("docker stats --no-stream --format json --no-trunc %s ", containerName);
        ExecuteResult result = ansibleService.execDocker(hostDTO, dockerRmCommand);
        if (result.failed()) {
            return null;
        }
        return result.getExecuteOut();
    }
}

