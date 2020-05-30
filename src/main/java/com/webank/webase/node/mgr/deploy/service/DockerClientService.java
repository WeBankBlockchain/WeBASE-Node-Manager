package com.webank.webase.node.mgr.deploy.service;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Component
public class DockerClientService {

    /**
     * Docker client map, key : ip:port; value: DockerClientObject.
     */
    private static final Map<String, DockerClient> DOCKER_CLIENT_MAP = new ConcurrentHashMap();

    /**
     *  Get container's name for node.
     *
     * @param nodePath
     * @return              delete all {@link File#separator} and blank of nodePath's absolute path.
     */
    public static String getContainerName(Path nodePath){
        return nodePath.toAbsolutePath().toString()
                .replaceAll(File.separator,"").replaceAll(" ","");
    }

    /**
     * Get a docker client to server, create one if no exists.
     *
     * @param ip
     * @param port
     * @return
     */
    public DockerClient getDockerClient(String ip, short port) {
        String key = String.format("%s:%s", ip, port);

        if (DOCKER_CLIENT_MAP.containsKey(key)) {
            return DOCKER_CLIENT_MAP.get(key);
        }

        // create new client
        DockerCmdExecFactory factory = new NettyDockerCmdExecFactory()
                .withReadTimeout(10 * 60 * 1000)
                .withConnectTimeout(10 * 60 * 1000);

        DefaultDockerClientConfig.Builder configBuilder = new DefaultDockerClientConfig.Builder()
                .withDockerTlsVerify(false)
                .withDockerHost(String.format("tcp://%s:%s", ip, port))
                .withApiVersion("1.23");

        DockerClient client = DockerClientBuilder.getInstance(configBuilder)
                .withDockerCmdExecFactory(factory)
                .build();
        DOCKER_CLIENT_MAP.put(key, client);

        return client;
    }

}