package com.webank.webase.node.mgr.deploy.service.docker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.webank.webase.node.mgr.base.properties.ConstantProperties;

import lombok.extern.log4j.Log4j2;

/**
 * TODO. 1. A host should exec only one command at the same time.
 */

@Log4j2
@Configuration
public class DockerClientService{
    @Autowired private ConstantProperties constantProperties;

    @Bean
    public DockerOptions dockerOptions() {
        if (constantProperties.isUseDockerSDK()){
            log.info("Use docker SDK.");
            return new DockerOptionsSDKImpl();
        }
        log.info("Use docker command.");
        return new DockerOptionsCmdImpl();
    }
}

