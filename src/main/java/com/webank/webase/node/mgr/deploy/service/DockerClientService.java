package com.webank.webase.node.mgr.deploy.service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.RestartPolicy;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import com.webank.webase.node.mgr.base.properties.ConstantProperties;

import lombok.extern.log4j.Log4j2;

/**
 * TODO. 1. A host should exec only one command at the same time.
 */

@Log4j2
@Component
public class DockerClientService {

    /**
     * Docker client map, key : ip:port; value: DockerClientObject.
     */
    private static final ConcurrentHashMap<String, DockerClient> DOCKER_CLIENT_CACHE = new ConcurrentHashMap<>();

    @Autowired private ConstantProperties constant;

    /**
     * Get container's name for node.
     *
     * @param rootDirOnHost
     * @param chainName
     * @param hostIndex
     * @return delete all {@link File#separator} and blank of node path on host.
     */
    public static String getContainerName(String rootDirOnHost, String chainName, short hostIndex) {
        return String.format("%s%snode%s",
                rootDirOnHost.replaceAll(File.separator, "").replaceAll(" ", ""),
                chainName,
                hostIndex);
    }


    /**
     * Pull image, maybe same tag but newer.
     *
     * @param ip
     * @param port
     * @param imageTag
     * @return
     */
    public boolean pullImage(String ip, short port, String imageTag) {
        String image = this.getImageRepositoryTag(imageTag);
        log.info("Host:[{}] pull image:[{}].", ip, image);
        try {
            // pull image, maybe same tag but newer
            DockerClient dockerClient = this.getDockerClient(ip, port);

            return dockerClient.pullImageCmd(image).start()
                    .awaitCompletion(constant.getDockerPullTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Host:[{}] docker pull image:[{}] error.", ip, image, e);
        }
        return false;
    }

    /**
     * Query container by container name;
     *
     * @param ip
     * @param port
     * @param containerName
     * @return
     */
    public Container getContainer(String ip, short port, String containerName) {
        log.info("Host:[{}] query container by name:[{}].", ip, containerName);
        try {
            DockerClient dockerClient = this.getDockerClient(ip, port);
            List<Container> containerList = dockerClient.listContainersCmd().withShowAll(true)
                    .withNameFilter(Arrays.asList(new String[]{containerName})).exec();
            if (CollectionUtils.size(containerList) > 0) {
                log.info("Host:[{}] exists container by name:[{}].", ip, containerName);
                return containerList.get(0);
            }
            log.info("Host:[{}] not exists container by name:[{}].", ip, containerName);
        } catch (Exception e) {
            log.info("Host:[{}] query container by name:[{}] error.", ip, containerName, e);
        }

        return null;
    }

    /**
     * @param ip
     * @param port
     * @param containerName
     * @param chainRootOnHost
     * @param nodeIndex
     * @return container id.
     */
    public String create(String ip,
                         short port,
                         String imageTag,
                         String containerName,
                         String chainRootOnHost,
                         Short nodeIndex) {
        log.info("Host:[{}] create container:[{}].", ip, containerName);
        String image = this.getImageRepositoryTag(imageTag);
        try {
            String nodeRootOnHost = PathService.getNodeRootOnHost(chainRootOnHost, nodeIndex);
            DockerClient dockerClient = this.getDockerClient(ip, port);


            log.info("Host:[{}] create container:[{}], check exists?", ip, containerName);
            Container container = this.getContainer(ip, port, containerName);
            if (container != null) {
                log.info("Host:[{}] exists container:[{}] when create, remove first.", ip, containerName);
                dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
            }

            Bind data = new Bind(nodeRootOnHost, new Volume("/data"));
            Bind yml = new Bind(String.format("%s/application.yml", nodeRootOnHost), new Volume("/front/conf/application-docker.yml"));
            Bind sdk = new Bind(String.format("%s/sdk", chainRootOnHost), new Volume("/data/sdk"));
            Bind frontLog = new Bind(String.format("%s/front-log", nodeRootOnHost), new Volume("/front/log"));

            log.info("Host:[{}] create container:[{}].", ip, image);
            RestartPolicy restartPolicy = RestartPolicy.onFailureRestart(3);
            CreateContainerResponse response = dockerClient
                    .createContainerCmd(image)
                    .withName(containerName)
                    .withWorkingDir("/data")
                    .withHostConfig(new HostConfig()
                            .withBinds(data, frontLog, yml, sdk)
                            .withRestartPolicy(restartPolicy)
                            .withNetworkMode("host"))
                    .withEnv("SPRING_PROFILES_ACTIVE=docker").exec();
            log.info("Host:[{}] create container:[{}] success with id:[{}].", ip, image, response.getId());
            return response.getId();
        } catch (Exception e) {
            log.info("Host:[{}] create container:[{}] error.", ip, image, e);
        }
        return null;
    }


    /**
     * @param ip
     * @param dockerPort
     * @param containerId
     * @return
     */
    public boolean startById(String ip, short dockerPort, String containerId) {
        log.info("Host:[{}] start container by id:[{}].", ip, containerId);
        try {
            DockerClient dockerClient = this.getDockerClient(ip, dockerPort);
            if (StringUtils.isBlank(containerId)) {
                return false;
            }
            dockerClient.startContainerCmd(containerId).exec();
            log.info("Host:[{}] start container:[{}] success.", ip, containerId);
            return true;
        } catch (Exception e) {
            log.info("Host:[{}] start container by id:[{}] error.", ip, containerId, e);
        }
        return false;
    }


    /**
     * @param ip
     * @param dockerPort
     * @param containerName
     * @return
     */
    public boolean removeByName(String ip, short dockerPort, String containerName) {
        log.info("Host:[{}] remove container by name:[{}].", ip, containerName);
        try {
            Container container = this.getContainer(ip, dockerPort, containerName);
            if (container == null){
                return false;
            }
            DockerClient dockerClient = this.getDockerClient(ip, dockerPort);
            dockerClient.removeContainerCmd(container.getId())
                    .withForce(true)
                    .exec();
            log.info("Host:[{}] remove container:[{}] success.", ip, containerName);
            return true;
        } catch (Exception e) {
            log.info("Host:[{}] remove container by name:[{}] error.", ip, containerName, e);
        }
        return false;
    }

    /**
     * Get a docker client to server, create one if no exists.
     *
     * @param originIp
     * @param originPort
     * @return
     */
    private DockerClient getDockerClient(final String originIp, short originPort) {
        // cache client
        log.info("Get docker client for:[{}:{}].", originIp, originPort);
        return DOCKER_CLIENT_CACHE.computeIfAbsent(originIp, k -> {
            String tcpUrl = String.format("tcp://%s:%s", originIp, originPort);

            if (MapUtils.isNotEmpty(constant.getDockerProxyMap())
                    && constant.getDockerProxyMap().containsKey(originIp)) {
                Pair<String, Short> proxyIpPort = constant.getDockerProxyMap().get(originIp);
                tcpUrl = String.format("tcp://%s:%s", proxyIpPort.getLeft(), proxyIpPort.getValue());
                log.info("Get docker client for:[{}:{}], use proxy address:[{}:{}].",
                        originIp, originPort, proxyIpPort.getLeft(), proxyIpPort.getValue());
            }

            DefaultDockerClientConfig.Builder configBuilder = new DefaultDockerClientConfig.Builder()
                    .withDockerTlsVerify(false)
                    .withDockerHost(tcpUrl)
                    .withApiVersion("1.23");

            DockerClient client = DockerClientBuilder.getInstance(configBuilder)
                    .withDockerCmdExecFactory(new NettyDockerCmdExecFactory()
                            .withReadTimeout(constant.getDockerClientReadTimeout())
                            .withConnectTimeout(constant.getDockerClientConnectTimeout()))
                    .build();

            return client;
        });
    }

    /**
     * @return
     */
    private String getImageRepositoryTag(String imageTag) {
        // image repository and tag
        String image = String.format("%s:%s", constant.getDockerRepository(), imageTag);
        if (StringUtils.isNotBlank(constant.getDockerRegistryMirror())) {
            // image with mirror
            image = String.format("%s/%s", constant.getDockerRegistryMirror(), image);
        }
        return image;
    }
}

