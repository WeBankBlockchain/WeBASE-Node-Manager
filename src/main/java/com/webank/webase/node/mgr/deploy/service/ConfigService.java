/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.node.mgr.deploy.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.node.mgr.base.enums.ConfigTypeEnum;
import com.webank.webase.node.mgr.deploy.entity.TbConfig;
import com.webank.webase.node.mgr.deploy.mapper.TbConfigMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ConfigService {

    @Autowired private TbConfigMapper tbConfigMapper;

    @Autowired private EncryptType encryptType;

    /**
     * Select config by type.
     *
     * @param update Update config when value is true.
     * @param type
     * @return
     */
    public List<TbConfig> selectConfigList(boolean update, ConfigTypeEnum type) {
        if (type == null) {
            return Collections.emptyList();
        }

        switch (type) {
            case DOCKER_IMAGE_TYPE:
                // update tags from docker hub registry
//                    if (StringUtils.isBlank(constants.getImageTagUpdateUrl())) {
//                        throw new NodeMgrException(ConstantCode.NO_DOCKER_TAG_UPDATE_URL_ERROR);
//                    }
//
//                    log.info("Fetch tag from: [{}]", constants.getImageTagUpdateUrl());
//                    ResponseEntity<ImageTag[]> responseEntity =
//                            this.genericRestTemplate.getForEntity(constants.getImageTagUpdateUrl(), ImageTag[].class);
//                    if (responseEntity == null
//                            || ArrayUtils.isEmpty(responseEntity.getBody())) {
//                        // docker hub api return empty
//                        throw new NodeMgrException(ConstantCode.UPDATE_DOCKER_TAG_ERROR);
//                    }

//
//                    log.info("Docker image tag update success, new tag count is: [{}].",
//                            CollectionUtils.size(configList));

                List<TbConfig> configList = tbConfigMapper.selectByType(type.getId());
                return filterByEncryptType(configList, encryptType.getEncryptType());
            default:
                break;
        }

        return Collections.emptyList();

    }

    private static List<TbConfig> filterByEncryptType(List<TbConfig> configList, int encryptType) {
        switch (encryptType) {
            case EncryptType.ECDSA_TYPE:
                return configList.stream().filter(config -> !StringUtils.endsWith(config.getConfigValue(), "-gm"))
                        .collect(Collectors.toList());
            case EncryptType.SM2_TYPE:
                return configList.stream().filter(config -> StringUtils.endsWith(config.getConfigValue(), "-gm"))
                        .collect(Collectors.toList());
        }
        return configList;
    }


    /**
     * Result of listing tags from hub.docker.com.
     */
    static class ImageTag {

        private String layer;
        private String name;

        public String getLayer() {
            return layer;
        }

        public String getName() {
            return name;
        }

        public void setLayer(String layer) {
            this.layer = layer;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}

